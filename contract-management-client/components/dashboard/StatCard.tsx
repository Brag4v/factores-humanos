import { type LucideIcon } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { cn } from '@/lib/utils'

interface StatCardProps {
  title: string
  value: number
  icon: LucideIcon
  description?: string
  isLoading?: boolean
  variant?: 'default' | 'warning' | 'danger' | 'success'
}

const iconColorMap = {
  default: 'text-indigo-600 bg-indigo-50',
  warning: 'text-amber-600 bg-amber-50',
  danger: 'text-red-600 bg-red-50',
  success: 'text-green-600 bg-green-50',
}

const valueColorMap = {
  default: 'text-foreground',
  warning: 'text-amber-600',
  danger: 'text-red-600',
  success: 'text-green-600',
}

export function StatCard({ title, value, icon: Icon, description, isLoading, variant = 'default' }: StatCardProps) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle as="p" className="text-sm font-medium text-muted-foreground">{title}</CardTitle>
        <div className={cn('flex h-9 w-9 items-center justify-center rounded-lg', iconColorMap[variant])}>
          <Icon className="h-4 w-4" />
        </div>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <Skeleton className="h-9 w-24" />
        ) : (
          <p className={cn('text-3xl font-bold tabular-nums', valueColorMap[variant])}>{value}</p>
        )}
        {description && <p className="mt-1 text-xs text-muted-foreground">{description}</p>}
      </CardContent>
    </Card>
  )
}
