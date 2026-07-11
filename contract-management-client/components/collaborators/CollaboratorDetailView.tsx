'use client'

import Link from 'next/link'
import { format } from 'date-fns'
import { ArrowLeft, Pencil, Mail, Phone, Building2, Briefcase, CalendarDays, Star } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip'
import { CollaboratorStatusBadge, EligibilityBadge } from '@/components/shared/StatusBadge'
import { EmptyState } from '@/components/shared/EmptyState'
import { CollaboratorContractsTab } from './CollaboratorContractsTab'
import { CollaboratorReviewsTab } from './CollaboratorReviewsTab'
import { useCollaboratorDetails } from '@/lib/hooks/useCollaborators'
import { PERFORMANCE_CATEGORY_LABELS } from '@/constants'

function InfoItem({ icon: Icon, label, value }: { icon: React.ElementType; label: string; value?: string | null }) {
  if (!value) return null
  return (
    <div className="flex items-center gap-2 text-sm">
      <Icon className="h-4 w-4 shrink-0 text-blue-600" />
      <span className="text-muted-foreground">{label}:</span>
      <span className="font-medium">{value}</span>
    </div>
  )
}

interface CollaboratorDetailViewProps {
  nationalId: string
}

export function CollaboratorDetailView({ nationalId }: CollaboratorDetailViewProps) {
  const { data, isLoading, isError } = useCollaboratorDetails(nationalId)

  if (isLoading) {
    return (
      <div className="space-y-6 p-6">
        <Skeleton className="h-8 w-48" />
        <Card>
          <CardContent className="pt-6 space-y-4">
            <Skeleton className="h-7 w-64" />
            <Skeleton className="h-4 w-40" />
            <div className="grid grid-cols-2 gap-3">
              {[...Array(6)].map((_, i) => <Skeleton key={i} className="h-4 w-full" />)}
            </div>
          </CardContent>
        </Card>
        <Skeleton className="h-64 w-full" />
      </div>
    )
  }

  if (isError || !data) {
    return (
      <div className="p-6">
        <EmptyState
          title="Collaborator not found"
          description="This collaborator doesn't exist or could not be loaded."
          action={{ label: 'Back to list', href: '/collaborators' }}
        />
      </div>
    )
  }

  const avgRating = data.averageRating ?? null

  return (
    <div className="space-y-6 p-6">
      {/* Page header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="sm" asChild>
            <Link href="/collaborators">
              <ArrowLeft className="h-4 w-4" />
              Back
            </Link>
          </Button>
          <h1 className="text-2xl font-bold tracking-tight">{data.fullName}</h1>
          <CollaboratorStatusBadge status={data.status} />
        </div>
        <Button variant="outline" size="sm" asChild>
          <Link href={`/collaborators/${nationalId}/edit`}>
            <Pencil className="h-4 w-4" />
            Edit
          </Link>
        </Button>
      </div>

      {/* Profile card */}
      <Card className="overflow-hidden border-2 border-blue-300 shadow-md">
<div className="h-3 bg-gradient-to-r from-blue-700 via-blue-500 to-blue-300" />
  <CardContent className="pt-5 pb-5 bg-blue-50/70">
          <div className="grid gap-6 sm:grid-cols-2">
            {/* Left column: identity + contact */}
            <div className="space-y-3">
              <div className="flex items-center gap-3">
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-blue-600 text-white font-bold text-lg shadow-md shadow-blue-200 select-none">
                  {data.firstName[0]}{data.lastName[0]}
                </div>
                <div>
                  <p className="font-semibold text-blue-900 text-base">{data.fullName}</p>
                  <p className="text-xs text-muted-foreground font-mono">{data.nationalId} · {data.employeeCode}</p>
                </div>
              </div>

              <InfoItem icon={Mail} label="Email" value={data.email} />
              <InfoItem icon={Phone} label="Phone" value={data.phone} />
              <InfoItem icon={Building2} label="Department" value={data.department} />
              <InfoItem icon={Briefcase} label="Position" value={data.position} />
              <InfoItem
                icon={CalendarDays}
                label="Hire Date"
                value={format(new Date(data.hireDate), 'MMM d, yyyy')}
              />
            </div>

            {/* Right column: performance summary */}
            <div className="space-y-4 sm:border-l-2 sm:border-blue-400 sm:pl-6">
              <div className="space-y-2">
                <p className="text-xs font-semibold uppercase tracking-wide text-blue-700">
                  Renewal Eligibility
                </p>
                <TooltipProvider>
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <span className="inline-block cursor-default">
                        <EligibilityBadge eligible={data.isEligibleForRenewal} />
                      </span>
                    </TooltipTrigger>
                    <TooltipContent>
                      Requires an average performance rating of 3.0 or higher
                    </TooltipContent>
                  </Tooltip>
                </TooltipProvider>
              </div>

              {avgRating !== null ? (
                <div className="space-y-1">
                  <p className="text-xs font-semibold uppercase tracking-wide text-blue-700">
                    Average Rating
                  </p>
                  <div className="flex items-center gap-2">
                    <Star className="h-4 w-4 text-amber-500 fill-amber-500" />
                    <span className="text-2xl font-bold tabular-nums">
                      {avgRating.toFixed(2)}
                    </span>
                    <span className="text-muted-foreground text-sm">/ 5.00</span>
                    <span className="text-xs text-muted-foreground">
                      ({data.totalReviews} review{data.totalReviews !== 1 ? 's' : ''})
                    </span>
                  </div>
                </div>
              ) : (
                <div className="space-y-1">
                  <p className="text-xs font-semibold uppercase tracking-wide text-blue-700">
                    Average Rating
                  </p>
                  <p className="text-sm text-muted-foreground italic">No reviews yet</p>
                </div>
              )}

              {data.latestReview && (
                <div className="space-y-1">
                  <p className="text-xs font-semibold uppercase tracking-wide text-blue-700">
                    Latest Review
                  </p>
                  <div className="rounded-md border bg-muted/30 p-3 text-sm space-y-1">
                    <p className="text-muted-foreground">
                      {format(new Date(data.latestReview.reviewPeriodStart), 'MMM d, yyyy')} –{' '}
                      {format(new Date(data.latestReview.reviewPeriodEnd), 'MMM d, yyyy')}
                    </p>
                    <div className="flex items-center gap-2">
                      <span
                        className={`font-semibold tabular-nums ${data.latestReview.rating >= 3 ? 'text-green-700' : 'text-red-600'}`}
                      >
                        {data.latestReview.rating.toFixed(2)}
                      </span>
                      <span className="text-muted-foreground">·</span>
                      <span>{PERFORMANCE_CATEGORY_LABELS[data.latestReview.performanceCategory]}</span>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Tabs */}
      <Tabs defaultValue="contracts">
        <TabsList>
          <TabsTrigger value="contracts">Contracts</TabsTrigger>
          <TabsTrigger value="reviews">Performance Reviews</TabsTrigger>
        </TabsList>

        <TabsContent value="contracts">
          <CollaboratorContractsTab
            collaboratorId={nationalId}
            isEligibleForRenewal={data.isEligibleForRenewal}
          />
        </TabsContent>

        <TabsContent value="reviews">
          <CollaboratorReviewsTab collaboratorId={nationalId} />
        </TabsContent>
      </Tabs>
    </div>
  )
}
