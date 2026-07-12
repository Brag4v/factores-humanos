import Link from 'next/link'
import { type LucideIcon, Inbox } from 'lucide-react'
import { Button } from '@/components/ui/button'

interface EmptyStateProps {
  icon?: LucideIcon
  title: string
  description?: string
  /** Heading level for the title. Defaults to 'h2' (empty state is normally the
   *  main content directly under a page's <h1>). Pass 'h3' when nesting inside a
   *  section that already has its own <h2>, to avoid skipping heading levels. */
  headingLevel?: 'h2' | 'h3' | 'h4'
  action?: {
    label: string
    onClick?: () => void
    href?: string
  }
}

export function EmptyState({
  icon: Icon = Inbox,
  title,
  description,
  headingLevel: Heading = 'h2',
  action,
}: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <div className="flex h-12 w-12 items-center justify-center rounded-full bg-muted">
        <Icon className="h-6 w-6 text-muted-foreground" />
      </div>
      <Heading className="mt-4 text-sm font-semibold">{title}</Heading>
      {description && <p className="mt-1 text-sm text-muted-foreground">{description}</p>}
      {action && (
        action.href ? (
          <Button className="mt-4" size="sm" asChild>
            <Link href={action.href}>{action.label}</Link>
          </Button>
        ) : (
          <Button onClick={action.onClick} className="mt-4" size="sm">
            {action.label}
          </Button>
        )
      )}
    </div>
  )
}
