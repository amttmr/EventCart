import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Bell, CheckCheck } from 'lucide-react'
import { EmptyState } from '../components/EmptyState'
import { ErrorBanner } from '../components/ErrorBanner'
import { LoadingBlock } from '../components/LoadingBlock'
import { StatusBadge } from '../components/StatusBadge'
import { getApiErrorMessage } from '../lib/apiClient'
import { eventCartApi } from '../lib/eventcartApi'
import { useSessionStore } from '../store/sessionStore'
import { formatDateTime } from '../utils/format'

export function NotificationsPage() {
  const queryClient = useQueryClient()
  const activeCustomerId = useSessionStore((state) => state.activeCustomerId)

  const notificationsQuery = useQuery({
    queryKey: ['notifications', activeCustomerId],
    queryFn: () => eventCartApi.listNotifications(activeCustomerId),
    refetchInterval: 5_000,
  })

  const markReadMutation = useMutation({
    mutationFn: (notificationId: string) => eventCartApi.markNotificationRead(notificationId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['notifications', activeCustomerId] })
      void queryClient.invalidateQueries({ queryKey: ['dashboard-notifications', activeCustomerId] })
    },
  })

  return (
    <section className="stack">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Notification-service</p>
          <h1>Customer notifications</h1>
        </div>
      </div>

      <section className="panel">
        <div className="panel-heading">
          <div>
            <h2>Notification history</h2>
            <p>Messages created from order, inventory, and payment events.</p>
          </div>
        </div>

        {notificationsQuery.isLoading ? <LoadingBlock /> : null}
        {notificationsQuery.isError ? (
          <ErrorBanner
            message={getApiErrorMessage(notificationsQuery.error)}
            onRetry={() => void notificationsQuery.refetch()}
          />
        ) : null}
        {!notificationsQuery.isLoading && notificationsQuery.data?.length === 0 ? (
          <EmptyState
            icon={<Bell size={24} />}
            title="No notifications"
            message="Place an order and wait for Kafka consumers to create notification records."
          />
        ) : null}

        <div className="notification-list">
          {notificationsQuery.data?.map((notification) => (
            <article className="notification-row" key={notification.notificationId}>
              <div>
                <div className="notification-title">
                  <strong>{notification.title}</strong>
                  <StatusBadge status={notification.status} />
                </div>
                <p>{notification.message}</p>
                <span>
                  {notification.channel} | {notification.type} | {formatDateTime(notification.createdAt)}
                </span>
              </div>
              <button
                className="icon-button"
                disabled={notification.status === 'READ' || markReadMutation.isPending}
                onClick={() => markReadMutation.mutate(notification.notificationId)}
                title="Mark read"
                type="button"
              >
                <CheckCheck aria-hidden="true" size={18} />
              </button>
            </article>
          ))}
        </div>

        {markReadMutation.isError ? <ErrorBanner message={getApiErrorMessage(markReadMutation.error)} /> : null}
      </section>
    </section>
  )
}
