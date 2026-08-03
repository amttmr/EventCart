import { useQuery } from '@tanstack/react-query'
import {
  Bell,
  Boxes,
  CheckCircle2,
  CircleDot,
  ClipboardList,
  CreditCard,
  PackageSearch,
  Send,
  ShoppingCart,
} from 'lucide-react'
import { useAuth } from '../auth/useAuth'
import { EmptyState } from '../components/EmptyState'
import { ErrorBanner } from '../components/ErrorBanner'
import { LoadingBlock } from '../components/LoadingBlock'
import { StatusBadge } from '../components/StatusBadge'
import { getApiErrorMessage } from '../lib/apiClient'
import { eventCartApi } from '../lib/eventcartApi'
import { useSessionStore } from '../store/sessionStore'
import { formatCurrency, formatDateTime } from '../utils/format'

const flowSteps = [
  { label: 'Catalog', detail: 'Product data', icon: PackageSearch },
  { label: 'Cart', detail: 'Customer snapshot', icon: ShoppingCart },
  { label: 'Order', detail: 'Mongo outbox', icon: ClipboardList },
  { label: 'Inventory', detail: 'Kafka reservation', icon: Boxes },
  { label: 'Payment', detail: 'Simulated provider', icon: CreditCard },
  { label: 'Notify', detail: 'Customer message', icon: Bell },
]

export function DashboardPage() {
  const auth = useAuth()
  const activeCustomerId = useSessionStore((state) => state.activeCustomerId)
  const selectedProductId = useSessionStore((state) => state.selectedProductId)
  const lastOrderId = useSessionStore((state) => state.lastOrderId)

  const productsQuery = useQuery({
    queryKey: ['dashboard-products'],
    queryFn: () => eventCartApi.searchProducts({ active: true, size: 5 }),
  })

  const cartQuery = useQuery({
    enabled: auth.isAuthenticated && auth.hasRole(['CUSTOMER', 'ADMIN']),
    queryKey: ['dashboard-cart', activeCustomerId],
    queryFn: () => eventCartApi.getCart(activeCustomerId),
  })

  const ordersQuery = useQuery({
    enabled: auth.isAuthenticated && auth.hasRole(['CUSTOMER', 'ADMIN', 'SUPPORT']),
    queryKey: ['dashboard-orders', activeCustomerId],
    queryFn: () => eventCartApi.listOrders(activeCustomerId),
    refetchInterval: 5_000,
  })

  const notificationsQuery = useQuery({
    enabled: auth.isAuthenticated && auth.hasRole(['CUSTOMER', 'ADMIN', 'SUPPORT']),
    queryKey: ['dashboard-notifications', activeCustomerId],
    queryFn: () => eventCartApi.listNotifications(activeCustomerId),
    refetchInterval: 5_000,
  })

  const latestOrder = ordersQuery.data?.[0]
  const unreadNotifications =
    notificationsQuery.data?.filter((notification) => notification.status === 'UNREAD').length ?? 0

  return (
    <section className="stack">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Real-time commerce workflow</p>
          <h1>Operations dashboard</h1>
        </div>
        <StatusBadge status={auth.status.toUpperCase()} />
      </div>

      {auth.error ? <ErrorBanner message={auth.error} /> : null}

      <div className="metric-grid">
        <div className="metric-tile">
          <PackageSearch aria-hidden="true" size={22} />
          <span>Active products</span>
          <strong>{productsQuery.data?.totalElements ?? 0}</strong>
        </div>
        <div className="metric-tile">
          <ShoppingCart aria-hidden="true" size={22} />
          <span>Cart items</span>
          <strong>{cartQuery.data?.totalItems ?? 0}</strong>
        </div>
        <div className="metric-tile">
          <ClipboardList aria-hidden="true" size={22} />
          <span>Customer orders</span>
          <strong>{ordersQuery.data?.length ?? 0}</strong>
        </div>
        <div className="metric-tile">
          <Bell aria-hidden="true" size={22} />
          <span>Unread notifications</span>
          <strong>{unreadNotifications}</strong>
        </div>
      </div>

      <section className="panel">
        <div className="panel-heading">
          <div>
            <h2>Application flow</h2>
            <p>Current service sequence used by the React console.</p>
          </div>
        </div>
        <div className="flow-strip">
          {flowSteps.map((step) => {
            const Icon = step.icon
            return (
              <div className="flow-step" key={step.label}>
                <Icon aria-hidden="true" size={20} />
                <strong>{step.label}</strong>
                <span>{step.detail}</span>
              </div>
            )
          })}
        </div>
      </section>

      <div className="split-layout">
        <section className="panel">
          <div className="panel-heading">
            <div>
              <h2>Recent products</h2>
              <p>Catalog-service data returned through the API Gateway.</p>
            </div>
          </div>
          {productsQuery.isLoading ? <LoadingBlock /> : null}
          {productsQuery.isError ? (
            <ErrorBanner
              message={getApiErrorMessage(productsQuery.error)}
              onRetry={() => void productsQuery.refetch()}
            />
          ) : null}
          <div className="data-list">
            {productsQuery.data?.content.map((product) => (
              <div className="data-row" key={product.id}>
                <div>
                  <strong>{product.name}</strong>
                  <span>{product.sku}</span>
                </div>
                <div className="row-meta">
                  <span>{formatCurrency(product.price, product.currency)}</span>
                  <StatusBadge status={product.active ? 'ACTIVE' : 'INACTIVE'} />
                </div>
              </div>
            ))}
          </div>
        </section>

        <section className="panel">
          <div className="panel-heading">
            <div>
              <h2>Workflow state</h2>
              <p>Useful IDs while debugging local flows.</p>
            </div>
          </div>
          <div className="state-list">
            <div>
              <CircleDot aria-hidden="true" size={18} />
              <span>Selected product</span>
              <strong>{selectedProductId || 'None selected'}</strong>
            </div>
            <div>
              <Send aria-hidden="true" size={18} />
              <span>Last order</span>
              <strong>{lastOrderId || latestOrder?.orderId || 'No order yet'}</strong>
            </div>
            <div>
              <CheckCircle2 aria-hidden="true" size={18} />
              <span>Latest status</span>
              <strong>{latestOrder?.status ?? 'Not available'}</strong>
            </div>
            <div>
              <ClipboardList aria-hidden="true" size={18} />
              <span>Latest update</span>
              <strong>{formatDateTime(latestOrder?.updatedAt)}</strong>
            </div>
          </div>
          {!auth.isAuthenticated ? (
            <EmptyState
              icon={<UserIcon />}
              title="Sign in to see customer data"
              message="Catalog browsing is public; carts, orders, and notifications require a token."
            />
          ) : null}
        </section>
      </div>
    </section>
  )
}

function UserIcon() {
  return <CheckCircle2 size={24} />
}
