import { useQuery } from '@tanstack/react-query'
import { Boxes, ClipboardList, CreditCard, Search } from 'lucide-react'
import { useEffect, useState } from 'react'
import { EmptyState } from '../components/EmptyState'
import { ErrorBanner } from '../components/ErrorBanner'
import { LoadingBlock } from '../components/LoadingBlock'
import { StatusBadge } from '../components/StatusBadge'
import { getApiErrorMessage } from '../lib/apiClient'
import { eventCartApi } from '../lib/eventcartApi'
import { useSessionStore } from '../store/sessionStore'
import { formatCurrency, formatDateTime } from '../utils/format'

export function OrdersPage() {
  const activeCustomerId = useSessionStore((state) => state.activeCustomerId)
  const lastOrderId = useSessionStore((state) => state.lastOrderId)
  const setLastOrderId = useSessionStore((state) => state.setLastOrderId)
  const [selectedOrderId, setSelectedOrderId] = useState(lastOrderId)

  useEffect(() => {
    if (lastOrderId) {
      setSelectedOrderId(lastOrderId)
    }
  }, [lastOrderId])

  const ordersQuery = useQuery({
    queryKey: ['orders', activeCustomerId],
    queryFn: () => eventCartApi.listOrders(activeCustomerId),
    refetchInterval: 4_000,
  })

  const orderQuery = useQuery({
    enabled: selectedOrderId.length > 0,
    queryKey: ['order', selectedOrderId],
    queryFn: () => eventCartApi.getOrder(selectedOrderId),
    refetchInterval: 4_000,
  })

  const reservationQuery = useQuery({
    enabled: selectedOrderId.length > 0,
    queryKey: ['reservation', selectedOrderId],
    queryFn: () => eventCartApi.getReservation(selectedOrderId),
    retry: false,
    refetchInterval: 4_000,
  })

  const paymentQuery = useQuery({
    enabled: selectedOrderId.length > 0,
    queryKey: ['payment', selectedOrderId],
    queryFn: () => eventCartApi.getPaymentByOrder(selectedOrderId),
    retry: false,
    refetchInterval: 4_000,
  })

  function selectOrder(orderId: string) {
    setSelectedOrderId(orderId)
    setLastOrderId(orderId)
  }

  return (
    <section className="stack">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Order-service</p>
          <h1>Order tracking</h1>
        </div>
      </div>

      <section className="panel">
        <form className="toolbar-form" onSubmit={(event) => event.preventDefault()}>
          <label>
            Order ID
            <input
              onChange={(event) => setSelectedOrderId(event.target.value.trim())}
              placeholder="Paste order id"
              value={selectedOrderId}
            />
          </label>
          <button
            className="primary-button"
            disabled={selectedOrderId.length === 0}
            onClick={() => void orderQuery.refetch()}
            type="button"
          >
            <Search aria-hidden="true" size={18} />
            Inspect
          </button>
        </form>
      </section>

      <div className="split-layout split-layout--orders">
        <section className="panel">
          <div className="panel-heading">
            <div>
              <h2>Customer orders</h2>
              <p>Auto-refreshes while Kafka events update order status.</p>
            </div>
          </div>

          {ordersQuery.isLoading ? <LoadingBlock /> : null}
          {ordersQuery.isError ? (
            <ErrorBanner message={getApiErrorMessage(ordersQuery.error)} onRetry={() => void ordersQuery.refetch()} />
          ) : null}
          {!ordersQuery.isLoading && ordersQuery.data?.length === 0 ? (
            <EmptyState
              icon={<ClipboardList size={24} />}
              title="No orders yet"
              message="Place an order from the cart to start the Kafka workflow."
            />
          ) : null}

          <div className="data-list">
            {ordersQuery.data?.map((order) => (
              <button
                className={order.orderId === selectedOrderId ? 'data-row data-row--selected' : 'data-row'}
                key={order.orderId}
                onClick={() => selectOrder(order.orderId)}
                type="button"
              >
                <div>
                  <strong>{order.orderId}</strong>
                  <span>{formatDateTime(order.createdAt)}</span>
                </div>
                <div className="row-meta">
                  <span>{formatCurrency(order.totalAmount, order.currency)}</span>
                  <StatusBadge status={order.status} />
                </div>
              </button>
            ))}
          </div>
        </section>

        <section className="panel">
          <div className="panel-heading">
            <div>
              <h2>Event state</h2>
              <p>Inventory and payment services write their own state from Kafka events.</p>
            </div>
          </div>

          {orderQuery.isLoading && selectedOrderId ? <LoadingBlock label="Loading order" /> : null}
          {orderQuery.isError ? <ErrorBanner message={getApiErrorMessage(orderQuery.error)} /> : null}

          {orderQuery.data ? (
            <>
              <div className="detail-header">
                <div>
                  <span>Order</span>
                  <strong>{orderQuery.data.orderId}</strong>
                </div>
                <StatusBadge status={orderQuery.data.status} />
              </div>
              <dl className="summary-list">
                <div>
                  <dt>Total</dt>
                  <dd>{formatCurrency(orderQuery.data.totalAmount, orderQuery.data.currency)}</dd>
                </div>
                <div>
                  <dt>Created</dt>
                  <dd>{formatDateTime(orderQuery.data.createdAt)}</dd>
                </div>
                <div>
                  <dt>Updated</dt>
                  <dd>{formatDateTime(orderQuery.data.updatedAt)}</dd>
                </div>
                <div>
                  <dt>Status reason</dt>
                  <dd>{orderQuery.data.statusReason || 'None'}</dd>
                </div>
              </dl>
              <div className="timeline">
                <div>
                  <ClipboardList aria-hidden="true" size={18} />
                  <span>Order</span>
                  <StatusBadge status={orderQuery.data.status} />
                </div>
                <div>
                  <Boxes aria-hidden="true" size={18} />
                  <span>Inventory</span>
                  {reservationQuery.data ? (
                    <StatusBadge status={reservationQuery.data.status} />
                  ) : (
                    <span className="muted-text">Pending or unavailable</span>
                  )}
                </div>
                <div>
                  <CreditCard aria-hidden="true" size={18} />
                  <span>Payment</span>
                  {paymentQuery.data ? (
                    <StatusBadge status={paymentQuery.data.status} />
                  ) : (
                    <span className="muted-text">Pending or unavailable</span>
                  )}
                </div>
              </div>
            </>
          ) : (
            <EmptyState
              icon={<Search size={24} />}
              title="Select an order"
              message="Choose an order from the list or paste an order ID to inspect the event results."
            />
          )}
        </section>
      </div>
    </section>
  )
}
