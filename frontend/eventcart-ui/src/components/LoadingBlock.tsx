import { LoaderCircle } from 'lucide-react'

type LoadingBlockProps = {
  label?: string
}

export function LoadingBlock({ label = 'Loading data' }: LoadingBlockProps) {
  return (
    <div className="loading-block" aria-live="polite">
      <LoaderCircle aria-hidden="true" className="spin" size={20} />
      <span>{label}</span>
    </div>
  )
}
