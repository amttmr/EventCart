export function formatCurrency(value: number | string | undefined, currency = 'INR') {
  const amount = typeof value === 'string' ? Number(value) : value
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency,
    maximumFractionDigits: 2,
  }).format(Number.isFinite(amount) ? Number(amount) : 0)
}

export function formatDateTime(value?: string) {
  if (!value) {
    return 'Not available'
  }

  return new Intl.DateTimeFormat('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

export function createIdempotencyKey(customerId: string) {
  return `${customerId}-order-${Date.now()}`
}
