'use client'

import Link from 'next/link'
import { useSearchParams } from 'next/navigation'
import { PlusCircle } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { DataTablePagination } from '@/components/shared/DataTablePagination'
import { ContractFilters } from './ContractFilters'
import { ContractsTable } from './ContractsTable'
import { useContracts } from '@/lib/hooks/useContracts'
import { DEFAULT_PAGE_SIZE } from '@/constants'
import type { ContractStatus, ContractType } from '@/lib/types'

export function ContractsView() {
  const searchParams = useSearchParams()

  const page = Number(searchParams.get('page') ?? '0')
  const status = (searchParams.get('status') as ContractStatus) || undefined
  const contractType = (searchParams.get('contractType') as ContractType) || undefined
  const collaboratorId = searchParams.get('collaboratorId') || undefined

  const { data, isLoading } = useContracts({
    page,
    size: DEFAULT_PAGE_SIZE,
    status,
    contractType,
    collaboratorId,
  })

  const contracts = data?.content ?? []
  const totalPages = data?.totalPages ?? 0
  const totalElements = data?.totalElements ?? 0

  function handlePageChange(newPage: number) {
    const params = new URLSearchParams(searchParams.toString())
    params.set('page', String(newPage))
    window.history.pushState(null, '', `?${params.toString()}`)
    window.dispatchEvent(new PopStateEvent('popstate'))
  }

  return (
    <div className="space-y-6 p-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Contracts</h1>
          <p className="text-sm text-muted-foreground">
            {isLoading ? 'Loading…' : `${totalElements} contract${totalElements !== 1 ? 's' : ''}`}
          </p>
        </div>
        <Button asChild>
          <Link href="/contracts/new">
            <PlusCircle className="h-4 w-4" />
            New Contract
          </Link>
        </Button>
      </div>

      {/* Filters */}
      <ContractFilters />

      {/* Table */}
      <Card>
        <CardContent className="p-0">
          <ContractsTable data={contracts} isLoading={isLoading} />
          {totalPages > 1 && (
            <DataTablePagination
              page={page}
              totalPages={totalPages}
              totalElements={totalElements}
              pageSize={DEFAULT_PAGE_SIZE}
              onPageChange={handlePageChange}
            />
          )}
        </CardContent>
      </Card>
    </div>
  )
}
