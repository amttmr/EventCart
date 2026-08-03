import { AlertTriangle, RefreshCw } from 'lucide-react'

type ErrorBannerProps = {
  message: string
  onRetry?: () => void
}

export function ErrorBanner({ message, onRetry }: ErrorBannerProps) {
  return (
    <div className="error-banner" role="alert">
      <AlertTriangle aria-hidden="true" size={18} />
      <span>{message}</span>
      {onRetry ? (
        <button className="icon-button" type="button" onClick={onRetry} title="Retry">
          <RefreshCw aria-hidden="true" size={16} />
        </button>
      ) : null}
    </div>
  )
}
