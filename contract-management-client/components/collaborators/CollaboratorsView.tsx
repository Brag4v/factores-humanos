'use client'

import Link from 'next/link'
import { useSearchParams, useRouter, usePathname } from 'next/navigation'
import { UserPlus } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { CollaboratorFilters } from './CollaboratorFilters'
import { CollaboratorsTable } from './CollaboratorsTable'
import { DataTablePagination } from '@/components/shared/DataTablePagination'
import { useCollaborators } from '@/lib/hooks/useCollaborators'
import { DEFAULT_PAGE_SIZE } from '@/constants'
import type { CollaboratorStatus } from '@/lib/types'

export function CollaboratorsView() {
  const router = useRouter()
  const pathname = usePathname()
  const searchParams = useSearchParams()

  const page = Number(searchParams.get('page') ?? '0')
  const search = searchParams.get('search') ?? undefined
  const status = (searchParams.get('status') as CollaboratorStatus) ?? undefined
  const department = searchParams.get('department') ?? undefined

  const { data, isLoading } = useCollaborators({
    page,
    size: DEFAULT_PAGE_SIZE,
    search,
    status,
    department,
  })

  const handlePageChange = (newPage: number) => {
    const params = new URLSearchParams(searchParams.toString())
    params.set('page', String(newPage))
    router.push(`${pathname}?${params.toString()}`)
  }

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Collaborators</h1>
          <p className="text-muted-foreground">Manage your workforce</p>
        </div>
        <Button asChild>
          <Link href="/collaborators/new">
            <UserPlus className="h-4 w-4" />
            Add Collaborator
          </Link>
        </Button>
      </div>

      {/* Filters */}
      <CollaboratorFilters />

      {/* Table */}
      <Card>
        <CardContent className="p-0">
          <div className="p-4">
            <CollaboratorsTable data={data?.content ?? []} isLoading={isLoading} />
          </div>

          {data && data.totalElements > 0 && (
            <>
              <div className="border-t" />
              <DataTablePagination
                page={page}
                totalPages={data.totalPages}
                totalElements={data.totalElements}
                pageSize={DEFAULT_PAGE_SIZE}
                onPageChange={handlePageChange}
              />
            </>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
