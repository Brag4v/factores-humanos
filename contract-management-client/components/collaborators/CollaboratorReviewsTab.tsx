'use client'

import { useState } from 'react'
import { format } from 'date-fns'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { DataTablePagination } from '@/components/shared/DataTablePagination'
import { EmptyState } from '@/components/shared/EmptyState'
import { AddReviewDialog } from './AddReviewDialog'
import { useCollaboratorReviews } from '@/lib/hooks/useCollaborators'
import { PERFORMANCE_CATEGORY_LABELS } from '@/constants'

function RatingCell({ rating }: { rating: number }) {
  const color = rating >= 3 ? 'text-green-700' : 'text-red-600'
  return <span className={`font-semibold tabular-nums ${color}`}>{rating.toFixed(2)}</span>
}

interface CollaboratorReviewsTabProps {
  collaboratorId: string
}

const PAGE_SIZE = 10

export function CollaboratorReviewsTab({ collaboratorId }: CollaboratorReviewsTabProps) {
  const [page, setPage] = useState(0)
  const { data, isLoading } = useCollaboratorReviews(collaboratorId, { page, size: PAGE_SIZE })

  if (isLoading) {
    return (
      <div className="space-y-2 pt-4">
        {[...Array(4)].map((_, i) => (
          <Skeleton key={i} className="h-12 w-full rounded-md" />
        ))}
      </div>
    )
  }

  const reviews = data?.content ?? []
  const totalPages = data?.totalPages ?? 0
  const totalElements = data?.totalElements ?? 0

  return (
    <div className="space-y-4 pt-2">
      <div className="flex items-center justify-between">
        <span className="text-sm text-muted-foreground">
          {totalElements} review{totalElements !== 1 ? 's' : ''}
        </span>
        <AddReviewDialog collaboratorId={collaboratorId} />
      </div>

      {!reviews.length ? (
        <EmptyState
          title="No reviews yet"
          description="No performance reviews have been recorded for this collaborator."
        />
      ) : (
        <>
          <div className="rounded-md border overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-muted/50">
                <tr>
                  <th className="px-4 py-3 text-left font-medium text-muted-foreground">Period</th>
                  <th className="px-4 py-3 text-left font-medium text-muted-foreground">Rating</th>
                  <th className="px-4 py-3 text-left font-medium text-muted-foreground">Category</th>
                  <th className="px-4 py-3 text-left font-medium text-muted-foreground">Reviewer</th>
                  <th className="px-4 py-3 text-left font-medium text-muted-foreground">Date</th>
                  <th className="px-4 py-3 text-left font-medium text-muted-foreground">Renewal</th>
                </tr>
              </thead>
              <tbody className="divide-y">
                {reviews.map((r) => (
                  <tr key={r.id} className="hover:bg-muted/30 transition-colors">
                    <td className="px-4 py-3 whitespace-nowrap text-muted-foreground">
                      {format(new Date(r.reviewPeriodStart), 'MMM d, yyyy')} –{' '}
                      {format(new Date(r.reviewPeriodEnd), 'MMM d, yyyy')}
                    </td>
                    <td className="px-4 py-3">
                      <RatingCell rating={r.rating} />
                    </td>
                    <td className="px-4 py-3">
                      <Badge variant="outline" className="text-xs">
                        {PERFORMANCE_CATEGORY_LABELS[r.performanceCategory]}
                      </Badge>
                    </td>
                    <td className="px-4 py-3">{r.reviewerName}</td>
                    <td className="px-4 py-3 text-muted-foreground whitespace-nowrap">
                      {format(new Date(r.createdAt), 'MMM d, yyyy')}
                    </td>
                    <td className="px-4 py-3">
                      {r.isEligibleRenewal ? (
                        <Badge className="border-0 bg-green-100 text-green-800 text-xs">Yes</Badge>
                      ) : (
                        <Badge className="border-0 bg-red-100 text-red-800 text-xs">No</Badge>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {totalPages > 1 && (
            <DataTablePagination
              page={page}
              totalPages={totalPages}
              totalElements={totalElements}
              pageSize={PAGE_SIZE}
              onPageChange={setPage}
            />
          )}
        </>
      )}
    </div>
  )
}
