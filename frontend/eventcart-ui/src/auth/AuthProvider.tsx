import Keycloak from 'keycloak-js'
import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { appConfig } from '../app/config'
import { setAccessTokenProvider } from '../lib/apiClient'
import { AuthContext, type AuthContextValue, type AuthStatus } from './AuthContext'

const mockAuth: AuthContextValue = {
  status: 'authenticated',
  token: undefined,
  username: 'local-customer',
  customerId: appConfig.defaultCustomerId,
  roles: ['CUSTOMER', 'ADMIN', 'SUPPORT'],
  isAuthenticated: true,
  authEnabled: false,
  hasRole: () => true,
  login: () => undefined,
  logout: () => undefined,
}

const keycloak = appConfig.authEnabled
  ? new Keycloak({
      url: appConfig.keycloak.url,
      realm: appConfig.keycloak.realm,
      clientId: appConfig.keycloak.clientId,
    })
  : undefined

function extractRoles(tokenParsed?: Keycloak.KeycloakTokenParsed): string[] {
  const realmRoles = tokenParsed?.realm_access?.roles
  return Array.isArray(realmRoles) ? realmRoles : []
}

function extractCustomerId(tokenParsed?: Keycloak.KeycloakTokenParsed): string {
  const rawCustomerId = tokenParsed?.customer_id
  return typeof rawCustomerId === 'string' && rawCustomerId.length > 0
    ? rawCustomerId
    : appConfig.defaultCustomerId
}

function extractUsername(tokenParsed?: Keycloak.KeycloakTokenParsed): string {
  return tokenParsed?.preferred_username ?? tokenParsed?.name ?? 'anonymous'
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<AuthStatus>(
    appConfig.authEnabled ? 'checking' : 'authenticated',
  )
  const [token, setToken] = useState<string | undefined>(undefined)
  const [roles, setRoles] = useState<string[]>(mockAuth.roles)
  const [username, setUsername] = useState<string>(mockAuth.username)
  const [customerId, setCustomerId] = useState<string>(appConfig.defaultCustomerId)
  const [error, setError] = useState<string | undefined>(undefined)

  const syncFromKeycloak = useCallback(() => {
    if (!keycloak) {
      return
    }

    setToken(keycloak.token)
    setRoles(extractRoles(keycloak.tokenParsed))
    setUsername(extractUsername(keycloak.tokenParsed))
    setCustomerId(extractCustomerId(keycloak.tokenParsed))
    setStatus(keycloak.authenticated ? 'authenticated' : 'anonymous')
  }, [])

  useEffect(() => {
    if (!appConfig.authEnabled || !keycloak) {
      setAccessTokenProvider(() => undefined)
      return undefined
    }

    let mounted = true
    setAccessTokenProvider(() => keycloak.token)

    keycloak
      .init({
        onLoad: 'check-sso',
        pkceMethod: 'S256',
        silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
      })
      .then(() => {
        if (mounted) {
          syncFromKeycloak()
        }
      })
      .catch((unknownError: unknown) => {
        if (!mounted) {
          return
        }
        setStatus('error')
        setError(
          unknownError instanceof Error
            ? unknownError.message
            : 'Unable to initialize Keycloak authentication.',
        )
      })

    const refreshTimer = window.setInterval(() => {
      if (keycloak.authenticated) {
        keycloak.updateToken(30).then(syncFromKeycloak).catch(() => {
          setStatus('error')
          setError('Unable to refresh Keycloak token.')
        })
      }
    }, 20_000)

    return () => {
      mounted = false
      window.clearInterval(refreshTimer)
    }
  }, [syncFromKeycloak])

  const login = useCallback(() => {
    keycloak?.login()
  }, [])

  const logout = useCallback(() => {
    keycloak?.logout({ redirectUri: window.location.origin })
  }, [])

  const hasRole = useCallback(
    (requiredRoles: string[]) => requiredRoles.some((role) => roles.includes(role)),
    [roles],
  )

  const value = useMemo<AuthContextValue>(() => {
    if (!appConfig.authEnabled) {
      return mockAuth
    }

    return {
      status,
      token,
      username,
      customerId,
      roles,
      isAuthenticated: status === 'authenticated',
      authEnabled: true,
      error,
      hasRole,
      login,
      logout,
    }
  }, [customerId, error, hasRole, login, logout, roles, status, token, username])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
