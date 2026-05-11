import Link from 'next/link'
import { format } from 'date-fns'
import { ArrowRight } from 'lucide-react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'
import { CONTRACT_TYPE_LABELS } from '@/constants'
import type { ExpiringContractResponse } from '@/lib/types'

interface ExpiryBadgeProps {
  days: number
}

function ExpiryBadge({ days }: ExpiryBadgeProps) {
  if (days <= 0) return <Badge variant="destructive">Expired</Badge>
  if (days <= 7)
    return (
      <Badge variant="destructive" className="tabular-nums">
        {days}d left
      </Badge>
    )
  if (days <= 30)
    return (
      <Badge className="bg-amber-100 text-amber-800 hover:bg-amber-100 border-amber-200 tabular-nums">
        {days}d left
      </Badge>
    )
  return <Badge variant="secondary">{days}d left</Badge>
}

interface EligibilityBadgeProps {
  eligible: boolean
}

function EligibilityBadge({ eligible }: EligibilityBadgeProps) {
  return eligible ? (
    <Badge className="bg-green-100 text-green-800 hover:bg-green-100 border-green-200">Eligible</Badge>
  ) : (
    <Badge className="bg-red-100 text-red-800 hover:bg-red-100 border-red-200">Not Eligible</Badge>
  )
}

interface ExpiringContractsTableProps {
  contracts: ExpiringContractResponse[]
  isLoading: boolean
}

export function ExpiringContractsTable({ contracts, isLoading }: ExpiringContractsTableProps) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-start justify-between">
        <div>
          <CardTitle>Contracts Expiring Soon</CardTitle>
          <CardDescription className="mt-1">Contracts expiring within the next 30 days</CardDescription>
        </div>
        <Button variant="outline" size="sm" asChild>
          <Link href="/contracts?status=ACTIVE">
            View all <ArrowRight className="ml-1 h-3 w-3" />
          </Link>
        </Button>
      </CardHeader>

      <CardContent>
        {isLoading ? (
          <div className="space-y-3">
            {Array.from({ length: 5 }).map((_, i) => (
              <Skeleton key={i} className="h-12 w-full" />
            ))}
          </div>
        ) : contracts.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <p className="text-sm font-medium text-muted-foreground">No contracts expiring in the next 30 days</p>
            <p className="mt-1 text-xs text-muted-foreground">All contracts are in good standing.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b">
                  {['Contract #', 'Collaborator', 'Type', 'End Date', 'Days Left', 'Renewal'].map((h) => (
                    <th key={h} className="pb-3 text-left font-medium text-muted-foreground">
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {contracts.map((c) => (
                  <tr
                    key={c.id}
                    className={cn(
                      'border-b last:border-0 transition-colors hover:bg-muted/40',
                      c.daysUntilExpiry <= 7 && 'bg-red-50/50'
                    )}
                  >
                    <td className="py-3 pr-4">
                      <Link
                        href={`/contracts/${c.id}`}
                        className="font-medium text-indigo-600 hover:text-indigo-800 hover:underline"
                      >
                        {c.contractNumber}
                      </Link>
                    </td>
                    <td className="py-3 pr-4">
                      <Link
                        href={`/collaborators/${c.collaboratorId}`}
                        className="font-medium hover:text-indigo-600 hover:underline"
                      >
                        {c.collaboratorName}
                      </Link>
                      <p className="text-xs text-muted-foreground">{c.collaboratorEmail}</p>
                    </td>
                    <td className="py-3 pr-4">
                      <Badge variant="outline">{CONTRACT_TYPE_LABELS[c.contractType]}</Badge>
                    </td>
                    <td className="py-3 pr-4 text-muted-foreground tabular-nums">
                      {format(new Date(c.endDate), 'MMM d, yyyy')}
                    </td>
                    <td className="py-3 pr-4">
                      <ExpiryBadge days={c.daysUntilExpiry} />
                    </td>
                    <td className="py-3">
                      <EligibilityBadge eligible={c.isEligibleForRenewal} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
