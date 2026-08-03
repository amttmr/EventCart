export type ApiResponse<T> = {
  success: boolean
  data: T
  message: string
  timestamp: string
}

export type ApiError = {
  code: string
  message: string
  path: string
  timestamp: string
  details: Record<string, string>
}

export type PageResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export type Product = {
  id: string
  sku: string
  name: string
  description?: string
  category: string
  price: number
  currency: string
  availableQuantity: number
  tags: string[]
  active: boolean
  version?: number
  createdAt?: string
  updatedAt?: string
}

export type CreateProductRequest = {
  sku: string
  name: string
  description?: string
  category: string
  price: number
  currency: string
  availableQuantity: number
  tags: string[]
}

export type CartItem = {
  productId: string
  sku: string
  productName: string
  unitPrice: number
  currency: string
  quantity: number
  lineTotal: number
}

export type Cart = {
  cartId: string
  customerId: string
  items: CartItem[]
  totalItems: number
  subtotal: number
  currency: string
  version?: number
  updatedAt?: string
}

export type OrderStatus =
  | 'CREATED'
  | 'INVENTORY_RESERVED'
  | 'INVENTORY_FAILED'
  | 'PAYMENT_COMPLETED'
  | 'PAYMENT_FAILED'

export type OrderItem = {
  productId: string
  sku: string
  productName: string
  unitPrice: number
  currency: string
  quantity: number
  lineTotal: number
}

export type Order = {
  orderId: string
  customerId: string
  items: OrderItem[]
  totalAmount: number
  currency: string
  status: OrderStatus
  statusReason?: string
  version?: number
  createdAt?: string
  updatedAt?: string
}

export type InventoryItem = {
  productId: string
  sku: string
  productName: string
  availableQuantity: number
  reservedQuantity: number
  version?: number
  updatedAt?: string
}

export type InventoryReservationStatus = 'RESERVED' | 'FAILED' | 'RELEASED'

export type InventoryReservation = {
  reservationId: string
  orderId: string
  customerId: string
  status: InventoryReservationStatus
  items: { productId: string; sku: string; quantity: number }[]
  totalAmount: number
  currency: string
  failureReason?: string
  version?: number
  createdAt?: string
  updatedAt?: string
}

export type PaymentStatus = 'COMPLETED' | 'FAILED'

export type PaymentAttempt = {
  paymentId: string
  orderId: string
  customerId: string
  status: PaymentStatus
  amount: number
  currency: string
  providerName: string
  providerTransactionId?: string
  failureReason?: string
  version?: number
  createdAt?: string
  updatedAt?: string
}

export type NotificationStatus = 'UNREAD' | 'READ'

export type Notification = {
  notificationId: string
  customerId: string
  orderId: string
  type: string
  channel: string
  status: NotificationStatus
  title: string
  message: string
  correlationId?: string
  createdAt?: string
  readAt?: string
}
