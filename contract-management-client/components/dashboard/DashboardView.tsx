'use client'

import { Users, FileText, AlertTriangle, Clock } from 'lucide-react'
import { StatCard } from '@/components/dashboard/StatCard'
import { ExpiringContractsTable } from '@/components/dashboard/ExpiringContractsTable'
import { AlertBanner } from '@/components/dashboard/AlertBanner'
import { QuickActions } from '@/components/dashboard/QuickActions'
import { useExpiringContracts, useActiveContractsCount } from '@/lib/hooks/useContracts'
import { useCollaboratorsCount } from '@/lib/hooks/useCollaborators'

export function DashboardView() {
  const { data: expiring = [], isLoading: loadingExpiring } = useExpiringContracts(30)
  const { data: collaboratorsCount, isLoading: loadingCollaborators } = useCollaboratorsCount()
  const { data: contractsCount, isLoading: loadingContracts } = useActiveContractsCount()

  const critical = expiring.filter((c) => c.daysUntilExpiry <= 7)

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Dashboard</h1>
        <p className="text-muted-foreground">Contract management overview</p>
      </div>

      {/* Critical alert banner */}
      {critical.length > 0 && <AlertBanner contracts={critical} />}

      {/* Stat cards */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          title="Active Collaborators"
          value={collaboratorsCount ?? 0}
          icon={Users}
          isLoading={loadingCollaborators}
          variant="success"
        />
        <StatCard
          title="Active Contracts"
          value={contractsCount ?? 0}
          icon={FileText}
          isLoading={loadingContracts}
        />
        <StatCard
          title="Expiring (30 days)"
          value={expiring.length}
          icon={AlertTriangle}
          isLoading={loadingExpiring}
          variant={expiring.length > 0 ? 'warning' : 'default'}
          description="Require attention soon"
        />
        <StatCard
          title="Critical (≤7 days)"
          value={critical.length}
          icon={Clock}
          isLoading={loadingExpiring}
          variant={critical.length > 0 ? 'danger' : 'default'}
          description="Immediate action required"
        />
      </div>

      {/* Expiring contracts table */}
      <ExpiringContractsTable contracts={expiring} isLoading={loadingExpiring} />

      {/* Quick actions */}
      <QuickActions />
    </div>
  )
}
