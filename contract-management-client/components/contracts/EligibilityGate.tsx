import Link from 'next/link'
import { AlertTriangle } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { RENEWAL_ELIGIBILITY_THRESHOLD } from '@/constants'

interface EligibilityGateProps {
  collaboratorId: string
  collaboratorName?: string
  averageRating: number | null
}

export function EligibilityGate({ collaboratorId, collaboratorName, averageRating }: EligibilityGateProps) {
  return (
    <Card className="border-red-200 bg-red-50">
      <CardContent className="pt-6">
        <div className="flex gap-4">
          <AlertTriangle className="h-6 w-6 text-red-600 shrink-0 mt-0.5" />
          <div className="space-y-3">
            <div>
              <p className="font-semibold text-red-800">
                {collaboratorName ?? 'This collaborator'} is not eligible for renewal
              </p>
              <p className="text-sm text-red-700 mt-1">
                A minimum average performance rating of{' '}
                <strong>{RENEWAL_ELIGIBILITY_THRESHOLD.toFixed(1)}</strong> is required.
              </p>
            </div>

            {averageRating !== null ? (
              <div className="rounded-md border border-red-200 bg-white/70 px-4 py-3 text-sm">
                <div className="flex items-center justify-between">
                  <span className="text-muted-foreground">Current average rating</span>
                  <span className="font-bold text-red-700 tabular-nums">
                    {averageRating.toFixed(2)} / 5.00
                  </span>
                </div>
                <div className="flex items-center justify-between mt-1">
                  <span className="text-muted-foreground">Minimum required</span>
                  <span className="font-semibold tabular-nums">
                    {RENEWAL_ELIGIBILITY_THRESHOLD.toFixed(2)} / 5.00
                  </span>
                </div>
              </div>
            ) : (
              <p className="text-sm text-red-700">No performance reviews have been recorded yet.</p>
            )}

            <Button variant="outline" size="sm" asChild className="border-red-300 text-red-700 hover:bg-red-100">
              <Link href={`/collaborators/${collaboratorId}?tab=reviews`}>
                Add a Performance Review →
              </Link>
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
