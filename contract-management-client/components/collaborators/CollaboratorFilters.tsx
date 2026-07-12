'use client'

import { useCallback } from 'react'
import { useRouter, useSearchParams, usePathname } from 'next/navigation'
import { Search, X } from 'lucide-react'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useDebounce } from '@/lib/hooks/useDebounce'
import { COLLABORATOR_STATUS_LABELS } from '@/constants'
import type { CollaboratorStatus } from '@/lib/types'

const STATUS_OPTIONS: CollaboratorStatus[] = ['ACTIVE', 'INACTIVE', 'ON_HOLD']

export function CollaboratorFilters() {
  const router = useRouter()
  const pathname = usePathname()
  const searchParams = useSearchParams()

  const search = searchParams.get('search') ?? ''
  const status = searchParams.get('status') ?? ''
  const department = searchParams.get('department') ?? ''

  const hasFilters = search || status || department

  const updateParam = useCallback(
    (key: string, value: string) => {
      const params = new URLSearchParams(searchParams.toString())
      if (value) {
        params.set(key, value)
      } else {
        params.delete(key)
      }
      params.delete('page')
      router.push(`${pathname}?${params.toString()}`)
    },
    [router, pathname, searchParams]
  )

  const debouncedSearch = useDebounce((value: string) => updateParam('search', value), 300)
  const debouncedDepartment = useDebounce((value: string) => updateParam('department', value), 300)

  const clearFilters = () => {
    router.push(pathname)
  }

  return (
    <div className="flex flex-wrap items-center gap-3">
      {/* Search */}
      <div className="grid min-w-[220px] flex-1 items-center">
        <label htmlFor="collaborator-search" className="sr-only">
          Search collaborators by name, ID, or email
        </label>
        <Input
          id="collaborator-search"
          placeholder="Search by name, ID, email…"
          defaultValue={search}
          onChange={(e) => debouncedSearch(e.target.value)}
          className="col-start-1 row-start-1 pl-8"
        />
        <Search
          aria-hidden="true"
          className="col-start-1 row-start-1 ml-2.5 h-4 w-4 justify-self-start text-muted-foreground"
        />
      </div>

      {/* Status filter */}
      <Select value={status} onValueChange={(v) => updateParam('status', v === 'ALL' ? '' : v)}>
        <SelectTrigger className="w-[150px]" aria-label="Filter by status">
          <SelectValue placeholder="All statuses" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="ALL">All statuses</SelectItem>
          {STATUS_OPTIONS.map((s) => (
            <SelectItem key={s} value={s}>
              {COLLABORATOR_STATUS_LABELS[s]}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      {/* Department filter */}
      <div className="min-w-[160px]">
        <label htmlFor="collaborator-department" className="sr-only">
          Filter by department
        </label>
        <Input
          id="collaborator-department"
          placeholder="Department…"
          defaultValue={department}
          onChange={(e) => debouncedDepartment(e.target.value)}
        />
      </div>

      {/* Clear */}
      {hasFilters && (
        <Button variant="ghost" size="sm" onClick={clearFilters} className="text-muted-foreground">
          <X className="mr-1.5 h-3.5 w-3.5" />
          Clear filters
        </Button>
      )}
    </div>
  )
}
