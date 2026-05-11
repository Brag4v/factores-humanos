'use client'

import { useRouter } from 'next/navigation'
import { format } from 'date-fns'
import {
  createColumnHelper,
  flexRender,
  getCoreRowModel,
  useReactTable,
} from '@tanstack/react-table'
import { Badge } from '@/components/ui/badge'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/shared/EmptyState'
import { ContractStatusBadge } from '@/components/shared/StatusBadge'
import { CONTRACT_TYPE_LABELS } from '@/constants'
import type { ContractResponse } from '@/lib/types'

const col = createColumnHelper<ContractResponse>()

function ExpiryCell({ days, status }: { days: number; status: ContractResponse['status'] }) {
  if (status !== 'ACTIVE') return <span className="text-muted-foreground text-xs">—</span>
  if (days <= 0) return <Badge className="border-0 bg-red-100 text-red-800">Expired</Badge>
  if (days <= 7) return <Badge className="border-0 bg-red-100 text-red-800">{days}d</Badge>
  if (days <= 30) return <Badge className="border-0 bg-amber-100 text-amber-800">{days}d</Badge>
  return <span className="text-sm tabular-nums">{days}d</span>
}

const columns = [
  col.accessor('contractNumber', {
    header: 'Contract #',
    cell: (info) => (
      <span className="font-mono text-xs text-indigo-600">{info.getValue()}</span>
    ),
  }),
  col.accessor('collaboratorId', {
    header: 'Collaborator',
    cell: (info) => (
      <span className="font-mono text-xs text-muted-foreground">{info.getValue()}</span>
    ),
  }),
  col.accessor('contractType', {
    header: 'Type',
    cell: (info) => <Badge variant="outline">{CONTRACT_TYPE_LABELS[info.getValue()]}</Badge>,
  }),
  col.accessor('startDate', {
    header: 'Start',
    cell: (info) => (
      <span className="text-sm whitespace-nowrap">{format(new Date(info.getValue()), 'MMM d, yyyy')}</span>
    ),
  }),
  col.accessor('endDate', {
    header: 'End',
    cell: (info) => (
      <span className="text-sm whitespace-nowrap">{format(new Date(info.getValue()), 'MMM d, yyyy')}</span>
    ),
  }),
  col.accessor('status', {
    header: 'Status',
    cell: (info) => <ContractStatusBadge status={info.getValue()} />,
  }),
  col.display({
    id: 'expiry',
    header: 'Expiry',
    cell: ({ row }) => (
      <ExpiryCell days={row.original.daysUntilExpiry} status={row.original.status} />
    ),
  }),
  col.accessor('autoRenewal', {
    header: 'Auto Renew',
    cell: (info) => (
      <span className="text-sm">{info.getValue() ? 'Yes' : 'No'}</span>
    ),
  }),
]

interface ContractsTableProps {
  data: ContractResponse[]
  isLoading: boolean
}

export function ContractsTable({ data, isLoading }: ContractsTableProps) {
  const router = useRouter()

  const table = useReactTable({
    data,
    columns,
    getCoreRowModel: getCoreRowModel(),
    manualPagination: true,
  })

  if (isLoading) {
    return (
      <div className="space-y-2">
        {[...Array(8)].map((_, i) => (
          <Skeleton key={i} className="h-12 w-full rounded-md" />
        ))}
      </div>
    )
  }

  if (!data.length) {
    return <EmptyState title="No contracts found" description="Try adjusting your filters." />
  }

  return (
    <div className="rounded-md border overflow-hidden">
      <Table>
        <TableHeader>
          {table.getHeaderGroups().map((hg) => (
            <TableRow key={hg.id} className="bg-muted/50 hover:bg-muted/50">
              {hg.headers.map((header) => (
                <TableHead key={header.id} className="font-medium">
                  {flexRender(header.column.columnDef.header, header.getContext())}
                </TableHead>
              ))}
            </TableRow>
          ))}
        </TableHeader>
        <TableBody>
          {table.getRowModel().rows.map((row) => (
            <TableRow
              key={row.id}
              className="cursor-pointer hover:bg-muted/40 transition-colors"
              onClick={() => router.push(`/contracts/${row.original.id}`)}
            >
              {row.getVisibleCells().map((cell) => (
                <TableCell key={cell.id}>
                  {flexRender(cell.column.columnDef.cell, cell.getContext())}
                </TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )
}
