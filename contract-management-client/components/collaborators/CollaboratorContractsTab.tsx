'use client'

import Link from 'next/link'
import { format } from 'date-fns'
import { PlusCircle } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/shared/EmptyState'
import { ContractStatusBadge } from '@/components/shared/StatusBadge'
import { CONTRACT_TYPE_LABELS } from '@/constants'
import { useContractByCollaborator } from '@/lib/hooks/useContracts'
import type { ContractResponse } from '@/lib/types'

function ExpiryBadge({ days, status }: { days: number; status: ContractResponse['status'] }) {
  if (status !== 'ACTIVE') return null
  if (days <= 7) return <Badge className="border-0 bg-red-100 text-red-800">{days}d left</Badge>
  if (days <= 30) return <Badge className="border-0 bg-amber-100 text-amber-800">{days}d left</Badge>
  return null
}

interface CollaboratorContractsTabProps {
  collaboratorId: string
  isEligibleForRenewal: boolean
}

export function CollaboratorContractsTab({ collaboratorId, isEligibleForRenewal }: CollaboratorContractsTabProps) {
  const { data: contracts, isLoading } = useContractByCollaborator(collaboratorId)

  if (isLoading) {
    return (
      <div className="space-y-2 pt-4">
        {[...Array(3)].map((_, i) => (
          <Skeleton key={i} className="h-12 w-full rounded-md" />
        ))}
      </div>
    )
  }

  return (
    <div className="space-y-4 pt-2">
      <div className="flex items-center justify-between">
        <span className="text-sm text-muted-foreground">
          {contracts?.length ?? 0} contract{contracts?.length !== 1 ? 's' : ''}
        </span>
        <Button size="sm" asChild>
          <Link href={`/contracts/new?collaboratorId=${collaboratorId}`}>
            <PlusCircle className="h-4 w-4" />
            Add Contract
          </Link>
        </Button>
      </div>

      {!contracts?.length ? (
        <EmptyState
          title="No contracts"
          description="This collaborator has no contracts yet."
        />
      ) : (
        <div className="rounded-md border overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-muted/50">
              <tr>
                <th className="px-4 py-3 text-left font-medium text-muted-foreground">Contract #</th>
                <th className="px-4 py-3 text-left font-medium text-muted-foreground">Type</th>
                <th className="px-4 py-3 text-left font-medium text-muted-foreground">Period</th>
                <th className="px-4 py-3 text-left font-medium text-muted-foreground">Status</th>
                <th className="px-4 py-3 text-left font-medium text-muted-foreground">Expiry</th>
                <th className="px-4 py-3 text-right font-medium text-muted-foreground">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {contracts.map((c) => {
                const canRenew = c.status === 'ACTIVE' && isEligibleForRenewal
                const canTerminate = c.status === 'ACTIVE' || c.status === 'PENDING'
                return (
                  <tr key={c.id} className="hover:bg-muted/30 transition-colors">
                    <td className="px-4 py-3 font-mono text-xs">
                      <Link href={`/contracts/${c.id}`} className="text-indigo-600 hover:underline">
                        {c.contractNumber}
                      </Link>
                    </td>
                    <td className="px-4 py-3">
                      <Badge variant="outline">{CONTRACT_TYPE_LABELS[c.contractType]}</Badge>
                    </td>
                    <td className="px-4 py-3 text-muted-foreground whitespace-nowrap">
                      {format(new Date(c.startDate), 'MMM d, yyyy')} –{' '}
                      {format(new Date(c.endDate), 'MMM d, yyyy')}
                    </td>
                    <td className="px-4 py-3">
                      <ContractStatusBadge status={c.status} />
                    </td>
                    <td className="px-4 py-3">
                      <ExpiryBadge days={c.daysUntilExpiry} status={c.status} />
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center justify-end gap-2">
                        <Button variant="ghost" size="sm" asChild>
                          <Link href={`/contracts/${c.id}`}>View</Link>
                        </Button>
                        <Button variant="ghost" size="sm" disabled={!canRenew} asChild={canRenew}>
                          {canRenew ? (
                            <Link href={`/contracts/${c.id}/renew`}>Renew</Link>
                          ) : (
                            <span>Renew</span>
                          )}
                        </Button>
                        <Button variant="ghost" size="sm" disabled={!canTerminate} asChild={canTerminate}>
                          {canTerminate ? (
                            <Link href={`/contracts/${c.id}/terminate`}>Terminate</Link>
                          ) : (
                            <span>Terminate</span>
                          )}
                        </Button>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
