import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { LoadingBlock } from '../components/LoadingBlock'
import { useAuth } from './useAuth'

type ProtectedRouteProps = {
  roles: string[]
  children: ReactNode
}

export function ProtectedRoute({ roles, children }: ProtectedRouteProps) {
  const auth = useAuth()

  if (auth.status === 'checking') {
    return <LoadingBlock label="Checking session" />
  }

  if (auth.status === 'error') {
    return (
      <section className="panel">
        <h2>Authentication unavailable</h2>
        <p>{auth.error}</p>
        <button type="button" className="primary-button" onClick={auth.login}>
          Retry login
        </button>
      </section>
    )
  }

  if (!auth.isAuthenticated) {
    return <Navigate to="/" replace />
  }

  if (!auth.hasRole(roles)) {
    return (
      <section className="panel">
        <h2>Access denied</h2>
        <p>Your current token does not include one of these roles: {roles.join(', ')}.</p>
      </section>
    )
  }

  return children
}
