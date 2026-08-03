import { createContext } from 'react'

export type AuthStatus = 'checking' | 'authenticated' | 'anonymous' | 'error'

export type AuthContextValue = {
  status: AuthStatus
  token?: string
  username: string
  customerId: string
  roles: string[]
  isAuthenticated: boolean
  authEnabled: boolean
  error?: string
  hasRole: (roles: string[]) => boolean
  login: () => void
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)
