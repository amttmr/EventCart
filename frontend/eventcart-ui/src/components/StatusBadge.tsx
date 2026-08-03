import clsx from 'clsx'

type StatusBadgeProps = {
  status?: string
}

function statusTone(status?: string) {
  if (!status) {
    return 'neutral'
  }

  if (status.includes('FAILED')) {
    return 'danger'
  }

  if (status.includes('COMPLETED') || status.includes('RESERVED') || status === 'READ') {
    return 'success'
  }

  if (status.includes('CREATED') || status === 'UNREAD') {
    return 'info'
  }

  return 'neutral'
}

export function StatusBadge({ status = 'UNKNOWN' }: StatusBadgeProps) {
  return <span className={clsx('status-badge', `status-badge--${statusTone(status)}`)}>{status}</span>
}
