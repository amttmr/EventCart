import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Minus, PackagePlus, Plus, ShoppingCart, Trash2 } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { EmptyState } from '../components/EmptyState'
import { ErrorBanner } from '../components/ErrorBanner'
import { LoadingBlock } from '../components/LoadingBlock'
import { getApiErrorMessage } from '../lib/apiClient'
import { eventCartApi } from '../lib/eventcartApi'
import { useSessionStore } from '../store/sessionStore'
import type { CartItem } from '../types/api'
import { createIdempotencyKey, formatCurrency } from '../utils/format'

const addItemSchema = z.object({
  productId: z.string().min(1, 'Product ID is required'),
  quantity: z.coerce.number().int().min(1, 'Quantity must be at least 1'),
})

type AddItemFormInput = z.input<typeof addItemSchema>
type AddItemForm = z.output<typeof addItemSchema>

export function CartPage() {
  const queryClient = useQueryClient()
  const activeCustomerId = useSessionStore((state) => state.activeCustomerId)
  const selectedProductId = useSessionStore((state) => state.selectedProductId)
  const setSelectedProductId = useSessionStore((state) => state.setSelectedProductId)
  const setLastOrderId = useSessionStore((state) => state.setLastOrderId)
  const [idempotencyKey, setIdempotencyKey] = useState(() => createIdempotencyKey(activeCustomerId))

  const addForm = useForm<AddItemFormInput, unknown, AddItemForm>({
    resolver: zodResolver(addItemSchema),
    defaultValues: {
      productId: selectedProductId,
      quantity: 1,
    },
  })

  useEffect(() => {
    addForm.setValue('productId', selectedProductId)
  }, [addForm, selectedProductId])

  useEffect(() => {
    setIdempotencyKey(createIdempotencyKey(activeCustomerId))
  }, [activeCustomerId])

  const cartQuery = useQuery({
    queryKey: ['cart', activeCustomerId],
    queryFn: () => eventCartApi.getCart(activeCustomerId),
  })

  const addMutation = useMutation({
    mutationFn: (form: AddItemForm) => eventCartApi.addCartItem(activeCustomerId, form.productId, form.quantity),
    onSuccess: (_cart, form) => {
      setSelectedProductId(form.productId)
      addForm.reset({ productId: form.productId, quantity: 1 })
      void queryClient.invalidateQueries({ queryKey: ['cart', activeCustomerId] })
      void queryClient.invalidateQueries({ queryKey: ['dashboard-cart', activeCustomerId] })
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ productId, quantity }: { productId: string; quantity: number }) =>
      eventCartApi.updateCartItem(activeCustomerId, productId, quantity),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['cart', activeCustomerId] })
      void queryClient.invalidateQueries({ queryKey: ['dashboard-cart', activeCustomerId] })
    },
  })

  const removeMutation = useMutation({
    mutationFn: (productId: string) => eventCartApi.removeCartItem(activeCustomerId, productId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['cart', activeCustomerId] })
      void queryClient.invalidateQueries({ queryKey: ['dashboard-cart', activeCustomerId] })
    },
  })

  const clearMutation = useMutation({
    mutationFn: () => eventCartApi.clearCart(activeCustomerId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['cart', activeCustomerId] })
      void queryClient.invalidateQueries({ queryKey: ['dashboard-cart', activeCustomerId] })
    },
  })

  const placeOrderMutation = useMutation({
    mutationFn: () => eventCartApi.placeOrder(activeCustomerId, idempotencyKey.trim()),
    onSuccess: (order) => {
      setLastOrderId(order.orderId)
      setIdempotencyKey(createIdempotencyKey(activeCustomerId))
      void queryClient.invalidateQueries({ queryKey: ['orders', activeCustomerId] })
      void queryClient.invalidateQueries({ queryKey: ['dashboard-orders', activeCustomerId] })
      void queryClient.invalidateQueries({ queryKey: ['cart', activeCustomerId] })
    },
  })

  function changeQuantity(item: CartItem, delta: number) {
    const nextQuantity = item.quantity + delta
    if (nextQuantity > 0) {
      updateMutation.mutate({ productId: item.productId, quantity: nextQuantity })
    }
  }

  return (
    <section className="stack">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Cart-service</p>
          <h1>Customer cart</h1>
        </div>
      </div>

      <section className="panel">
        <form className="toolbar-form" onSubmit={addForm.handleSubmit((form) => addMutation.mutate(form))}>
          <label>
            Product ID
            <input placeholder="MongoDB product id" {...addForm.register('productId')} />
            {addForm.formState.errors.productId ? (
              <small>{addForm.formState.errors.productId.message}</small>
            ) : null}
          </label>
          <label>
            Quantity
            <input min={1} type="number" {...addForm.register('quantity')} />
            {addForm.formState.errors.quantity ? <small>{addForm.formState.errors.quantity.message}</small> : null}
          </label>
          <button className="primary-button" disabled={addMutation.isPending} type="submit">
            <PackagePlus aria-hidden="true" size={18} />
            Add item
          </button>
        </form>
        {addMutation.isError ? <ErrorBanner message={getApiErrorMessage(addMutation.error)} /> : null}
      </section>

      <div className="split-layout split-layout--cart">
        <section className="panel">
          <div className="panel-heading">
            <div>
              <h2>Cart items</h2>
              <p>Cart stores product snapshots, not live product references.</p>
            </div>
            <button
              className="ghost-button"
              disabled={!cartQuery.data?.items.length || clearMutation.isPending}
              onClick={() => clearMutation.mutate()}
              type="button"
            >
              <Trash2 aria-hidden="true" size={18} />
              Clear
            </button>
          </div>

          {cartQuery.isLoading ? <LoadingBlock /> : null}
          {cartQuery.isError ? (
            <ErrorBanner message={getApiErrorMessage(cartQuery.error)} onRetry={() => void cartQuery.refetch()} />
          ) : null}
          {!cartQuery.isLoading && cartQuery.data?.items.length === 0 ? (
            <EmptyState
              icon={<ShoppingCart size={24} />}
              title="Cart is empty"
              message="Add a catalog product before placing an order."
            />
          ) : null}

          <div className="cart-lines">
            {cartQuery.data?.items.map((item) => (
              <div className="cart-line" key={item.productId}>
                <div>
                  <strong>{item.productName}</strong>
                  <span>{item.sku}</span>
                </div>
                <div className="quantity-control">
                  <button
                    className="icon-button"
                    disabled={item.quantity <= 1 || updateMutation.isPending}
                    onClick={() => changeQuantity(item, -1)}
                    title="Decrease quantity"
                    type="button"
                  >
                    <Minus aria-hidden="true" size={16} />
                  </button>
                  <span>{item.quantity}</span>
                  <button
                    className="icon-button"
                    disabled={updateMutation.isPending}
                    onClick={() => changeQuantity(item, 1)}
                    title="Increase quantity"
                    type="button"
                  >
                    <Plus aria-hidden="true" size={16} />
                  </button>
                </div>
                <strong>{formatCurrency(item.lineTotal, item.currency)}</strong>
                <button
                  className="icon-button"
                  disabled={removeMutation.isPending}
                  onClick={() => removeMutation.mutate(item.productId)}
                  title="Remove item"
                  type="button"
                >
                  <Trash2 aria-hidden="true" size={16} />
                </button>
              </div>
            ))}
          </div>
        </section>

        <section className="panel">
          <div className="panel-heading">
            <div>
              <h2>Place order</h2>
              <p>Order-service reads the cart and publishes the first Kafka event.</p>
            </div>
          </div>
          <dl className="summary-list">
            <div>
              <dt>Customer</dt>
              <dd>{activeCustomerId}</dd>
            </div>
            <div>
              <dt>Total items</dt>
              <dd>{cartQuery.data?.totalItems ?? 0}</dd>
            </div>
            <div>
              <dt>Subtotal</dt>
              <dd>{formatCurrency(cartQuery.data?.subtotal, cartQuery.data?.currency ?? 'INR')}</dd>
            </div>
          </dl>
          <label className="field-block">
            Idempotency key
            <input onChange={(event) => setIdempotencyKey(event.target.value)} value={idempotencyKey} />
          </label>
          <button
            className="primary-button primary-button--wide"
            disabled={!cartQuery.data?.items.length || placeOrderMutation.isPending}
            onClick={() => placeOrderMutation.mutate()}
            type="button"
          >
            <ShoppingCart aria-hidden="true" size={18} />
            Place order
          </button>
          {placeOrderMutation.isSuccess ? (
            <div className="success-note">Order created: {placeOrderMutation.data.orderId}</div>
          ) : null}
          {placeOrderMutation.isError ? <ErrorBanner message={getApiErrorMessage(placeOrderMutation.error)} /> : null}
        </section>
      </div>
    </section>
  )
}
