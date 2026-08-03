import { create } from 'zustand'
import { appConfig } from '../app/config'

type SessionState = {
  activeCustomerId: string
  selectedProductId: string
  lastOrderId: string
  setActiveCustomerId: (customerId: string) => void
  setSelectedProductId: (productId: string) => void
  setLastOrderId: (orderId: string) => void
}

export const useSessionStore = create<SessionState>((set) => ({
  activeCustomerId: appConfig.defaultCustomerId,
  selectedProductId: '',
  lastOrderId: '',
  setActiveCustomerId: (activeCustomerId) => set({ activeCustomerId }),
  setSelectedProductId: (selectedProductId) => set({ selectedProductId }),
  setLastOrderId: (lastOrderId) => set({ lastOrderId }),
}))
