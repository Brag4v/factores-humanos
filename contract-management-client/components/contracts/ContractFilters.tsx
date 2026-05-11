'use client'

import { useCallback } from 'react'
import { useRouter, usePathname, useSearchParams } from 'next/navigation'
import { X } from 'lucide-react'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Badge } from '@/components/ui/badge'
import { useDebounce } from '@/lib/hooks/useDebounce'
import { CONTRACT_STATUS_LABELS, CONTRACT_TYPE_LABELS } from '@/constants'
import type { ContractStatus, ContractType } from '@/lib/types'

const STATUS_OPTIONS: ContractStatus[] = ['ACTIVE', 'PENDING', 'EXPIRED', 'RENEWED', 'TERMINATED']
const TYPE_OPTIONS: ContractType[] = ['FULL_TIME', 'PART_TIME', 'CONTRACTOR', 'TEMPORARY']

export function ContractFilters() {
  const router = useRouter()
  const pathname = usePathname()
  const searchParams = useSearchParams()

  const status = searchParams.get('status') ?? ''
  const contractType = searchParams.get('contractType') ?? ''
  const collaboratorId = searchParams.get('collaboratorId') ?? ''

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

  const handleCollaboratorSearch = useDebounce((value: string) => {
    updateParam('collaboratorId', value)
  }, 300)

  const clearAll = () => router.push(pathname)

  const hasFilters = !!(status || contractType || collaboratorId)

  const activeChips: { label: string; key: string }[] = [
    ...(status ? [{ label: CONTRACT_STATUS_LABELS[status as ContractStatus], key: 'status' }] : []),
    ...(contractType ? [{ label: CONTRACT_TYPE_LABELS[contractType as ContractType], key: 'contractType' }] : []),
    ...(collaboratorId ? [{ label: `Collaborator: ${collaboratorId}`, key: 'collaboratorId' }] : []),
  ]

  return (
    <div className="space-y-3">
      <div className="flex flex-wrap gap-3">
        {/* Status */}
        <Select value={status} onValueChange={(v) => updateParam('status', v === '__clear__' ? '' : v)}>
          <SelectTrigger className="w-40">
            <SelectValue placeholder="All Statuses" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="__clear__">All Statuses</SelectItem>
            {STATUS_OPTIONS.map((s) => (
              <SelectItem key={s} value={s}>
                {CONTRACT_STATUS_LABELS[s]}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        {/* Contract Type */}
        <Select value={contractType} onValueChange={(v) => updateParam('contractType', v === '__clear__' ? '' : v)}>
          <SelectTrigger className="w-44">
            <SelectValue placeholder="All Types" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="__clear__">All Types</SelectItem>
            {TYPE_OPTIONS.map((t) => (
              <SelectItem key={t} value={t}>
                {CONTRACT_TYPE_LABELS[t]}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        {/* Collaborator search */}
        <Input
          placeholder="Search by collaborator ID…"
          defaultValue={collaboratorId}
          onChange={(e) => handleCollaboratorSearch(e.target.value)}
          className="w-56"
        />

        {hasFilters && (
          <Button variant="ghost" size="sm" onClick={clearAll} className="text-muted-foreground">
            Clear all
          </Button>
        )}
      </div>

      {/* Active filter chips */}
      {activeChips.length > 0 && (
        <div className="flex flex-wrap gap-2">
          {activeChips.map((chip) => (
            <Badge
              key={chip.key}
              variant="secondary"
              className="cursor-pointer gap-1 pr-1.5"
              onClick={() => updateParam(chip.key, '')}
            >
              {chip.label}
              <X className="h-3 w-3" />
            </Badge>
          ))}
        </div>
      )}
    </div>
  )
}
