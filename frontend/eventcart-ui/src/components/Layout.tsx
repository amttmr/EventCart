import {
  Bell,
  Boxes,
  ClipboardList,
  LayoutDashboard,
  LogIn,
  LogOut,
  PackageSearch,
  ShieldCheck,
  ShoppingCart,
  UserRound,
} from 'lucide-react'
import { useEffect } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { useSessionStore } from '../store/sessionStore'

const navigation = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/catalog', label: 'Catalog', icon: PackageSearch },
  { to: '/cart', label: 'Cart', icon: ShoppingCart, roles: ['CUSTOMER', 'ADMIN'] },
  { to: '/orders', label: 'Orders', icon: ClipboardList, roles: ['CUSTOMER', 'ADMIN', 'SUPPORT'] },
  { to: '/notifications', label: 'Notifications', icon: Bell, roles: ['CUSTOMER', 'ADMIN', 'SUPPORT'] },
  { to: '/admin', label: 'Admin', icon: ShieldCheck, roles: ['ADMIN', 'SUPPORT'] },
]

function canSeeNavigationItem(authRoles: string[] | undefined, hasRole: (roles: string[]) => boolean) {
  return authRoles ? hasRole(authRoles) : true
}

export function Layout() {
  const auth = useAuth()
  const activeCustomerId = useSessionStore((state) => state.activeCustomerId)
  const setActiveCustomerId = useSessionStore((state) => state.setActiveCustomerId)
  const customerCanSwitch = auth.hasRole(['ADMIN', 'SUPPORT'])

  useEffect(() => {
    if (auth.isAuthenticated && !customerCanSwitch && activeCustomerId !== auth.customerId) {
      setActiveCustomerId(auth.customerId)
    }
  }, [activeCustomerId, auth.customerId, auth.isAuthenticated, customerCanSwitch, setActiveCustomerId])

  return (
    <div className="app-shell">
      <aside className="sidebar" aria-label="Application navigation">
        <div className="brand-lockup">
          <Boxes aria-hidden="true" size={28} />
          <div>
            <strong>EventCart</strong>
            <span>Order Console</span>
          </div>
        </div>

        <nav className="nav-list">
          {navigation
            .filter((item) => canSeeNavigationItem(item.roles, auth.hasRole))
            .map((item) => {
              const Icon = item.icon
              return (
                <NavLink
                  className={({ isActive }) => (isActive ? 'nav-link nav-link--active' : 'nav-link')}
                  key={item.to}
                  to={item.to}
                  title={item.label}
                >
                  <Icon aria-hidden="true" size={18} />
                  <span>{item.label}</span>
                </NavLink>
              )
            })}
        </nav>
      </aside>

      <div className="workspace">
        <header className="topbar">
          <div className="customer-control">
            <label htmlFor="active-customer">Customer</label>
            <input
              disabled={!customerCanSwitch}
              id="active-customer"
              onChange={(event) => setActiveCustomerId(event.target.value)}
              value={activeCustomerId}
            />
          </div>

          <div className="user-summary">
            <UserRound aria-hidden="true" size={18} />
            <div>
              <strong>{auth.username}</strong>
              <span>{auth.roles.length > 0 ? auth.roles.join(', ') : 'anonymous'}</span>
            </div>
            {auth.isAuthenticated ? (
              <button className="icon-button" type="button" onClick={auth.logout} title="Sign out">
                <LogOut aria-hidden="true" size={18} />
              </button>
            ) : (
              <button className="primary-button" type="button" onClick={auth.login}>
                <LogIn aria-hidden="true" size={18} />
                Sign in
              </button>
            )}
          </div>
        </header>

        <main className="page">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
