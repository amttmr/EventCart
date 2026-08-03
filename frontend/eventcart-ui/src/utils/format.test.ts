import { describe, expect, it } from 'vitest'
import { createIdempotencyKey, formatDateTime } from './format'

describe('format utilities', () => {
  it('creates an idempotency key with the customer prefix', () => {
    expect(createIdempotencyKey('customer-1')).toContain('customer-1-order-')
  })

  it('returns a stable fallback for empty dates', () => {
    expect(formatDateTime()).toBe('Not available')
  })
})
