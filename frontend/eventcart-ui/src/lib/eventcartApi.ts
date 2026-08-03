import { apiClient, unwrap } from './apiClient'
import type {
  Cart,
  CreateProductRequest,
  InventoryItem,
  InventoryReservation,
  Notification,
  Order,
  PageResponse,
  PaymentAttempt,
  Product,
} from '../types/api'

export type ProductSearchParams = {
  keyword?: string
  category?: string
  active?: boolean
  page?: number
  size?: number
}

export const eventCartApi = {
  searchProducts: (params: ProductSearchParams) =>
    unwrap<PageResponse<Product>>(apiClient.get('/products', { params })),

  createProduct: (body: CreateProductRequest) =>
    unwrap<Product>(apiClient.post('/products', body)),

  getCart: (customerId: string) => unwrap<Cart>(apiClient.get(`/carts/${customerId}`)),

  addCartItem: (customerId: string, productId: string, quantity: number) =>
    unwrap<Cart>(apiClient.post(`/carts/${customerId}/items`, { productId, quantity })),

  updateCartItem: (customerId: string, productId: string, quantity: number) =>
    unwrap<Cart>(apiClient.put(`/carts/${customerId}/items/${productId}`, { quantity })),

  removeCartItem: (customerId: string, productId: string) =>
    unwrap<Cart>(apiClient.delete(`/carts/${customerId}/items/${productId}`)),

  clearCart: (customerId: string) => unwrap<Cart>(apiClient.delete(`/carts/${customerId}`)),

  placeOrder: (customerId: string, idempotencyKey: string) =>
    unwrap<Order>(apiClient.post('/orders', { customerId, idempotencyKey })),

  listOrders: (customerId: string) =>
    unwrap<Order[]>(apiClient.get(`/orders/customer/${customerId}`)),

  getOrder: (orderId: string) => unwrap<Order>(apiClient.get(`/orders/${orderId}`)),

  upsertInventory: (
    productId: string,
    body: { sku: string; productName: string; availableQuantity: number },
  ) => unwrap<InventoryItem>(apiClient.put(`/inventory/${productId}`, body)),

  getInventory: (productId: string) => unwrap<InventoryItem>(apiClient.get(`/inventory/${productId}`)),

  getReservation: (orderId: string) =>
    unwrap<InventoryReservation>(apiClient.get(`/inventory/reservations/${orderId}`)),

  getPaymentByOrder: (orderId: string) =>
    unwrap<PaymentAttempt>(apiClient.get(`/payments/orders/${orderId}`)),

  listNotifications: (customerId: string) =>
    unwrap<Notification[]>(apiClient.get(`/notifications/customers/${customerId}`)),

  markNotificationRead: (notificationId: string) =>
    unwrap<Notification>(apiClient.put(`/notifications/${notificationId}/read`)),
}
