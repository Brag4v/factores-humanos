'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { format } from 'date-fns'
import { toast } from 'sonner'
import {
  createColumnHelper,
  flexRender,
  getCoreRowModel,
  useReactTable,
} from '@tanstack/react-table'
import { MoreHorizontal, Eye, Pencil, Trash2 } from 'lucide-react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Skeleton } from '@/components/ui/skeleton'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { CollaboratorStatusBadge } from '@/components/shared/StatusBadge'
import { EmptyState } from '@/components/shared/EmptyState'
import { useDeleteCollaborator } from '@/lib/hooks/useCollaborators'
import type { CollaboratorResponse } from '@/lib/types'

const col = createColumnHelper<CollaboratorResponse>()

interface CollaboratorsTableProps {
  data: CollaboratorResponse[]
  isLoading: boolean
}

export function CollaboratorsTable({ data, isLoading }: CollaboratorsTableProps) {
  const router = useRouter()
  const [deleteTarget, setDeleteTarget] = useState<CollaboratorResponse | null>(null)
  const { mutate: deleteCollaborator, isPending: isDeleting } = useDeleteCollaborator()

  const columns = [
    col.accessor('nationalId', {
      header: 'National ID',
      cell: (info) => <span className="font-mono text-xs">{info.getValue()}</span>,
    }),
    col.accessor('fullName', {
      header: 'Full Name',
      cell: (info) => <span className="font-medium">{info.getValue()}</span>,
    }),
    col.accessor('department', {
      header: 'Department',
      cell: (info) => info.getValue() ?? <span className="text-muted-foreground">—</span>,
    }),
    col.accessor('position', {
      header: 'Position',
    }),
    col.accessor('status', {
      header: 'Status',
      cell: (info) => <CollaboratorStatusBadge status={info.getValue()} />,
    }),
    col.accessor('hireDate', {
      header: 'Hire Date',
      cell: (info) => (
        <span className="tabular-nums text-muted-foreground">
          {format(new Date(info.getValue()), 'MMM d, yyyy')}
        </span>
      ),
    }),
    col.display({
      id: 'actions',
      header: () => <span className="sr-only">Actions</span>,
      cell: ({ row }) => {
        const collaborator = row.original
        return (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="h-8 w-8">
                <MoreHorizontal className="h-4 w-4" />
                <span className="sr-only">Open menu</span>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={() => router.push(`/collaborators/${collaborator.nationalId}`)}>
                <Eye className="h-4 w-4" />
                View
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => router.push(`/collaborators/${collaborator.nationalId}/edit`)}>
                <Pencil className="h-4 w-4" />
                Edit
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem
                className="text-destructive focus:text-destructive"
                onClick={() => setDeleteTarget(collaborator)}
              >
                <Trash2 className="h-4 w-4" />
                Delete
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        )
      },
    }),
  ]

  const table = useReactTable({
    data,
    columns,
    getCoreRowModel: getCoreRowModel(),
    manualPagination: true,
  })

  const handleConfirmDelete = () => {
    if (!deleteTarget) return
    deleteCollaborator(deleteTarget.nationalId, {
      onSuccess: () => {
        toast.success(`${deleteTarget.fullName} has been deleted.`)
        setDeleteTarget(null)
      },
      onError: (err) => {
        toast.error(err.message)
      },
    })
  }

  if (isLoading) {
    return (
      <div className="space-y-2">
        {Array.from({ length: 8 }).map((_, i) => (
          <Skeleton key={i} className="h-12 w-full" />
        ))}
      </div>
    )
  }

  if (data.length === 0) {
    return (
      <EmptyState
        title="No collaborators found"
        description="Try adjusting your filters or add a new collaborator."
      />
    )
  }

  return (
    <>
      <Table>
        <TableHeader>
          {table.getHeaderGroups().map((hg) => (
            <TableRow key={hg.id}>
              {hg.headers.map((header) => (
                <TableHead key={header.id}>
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
              className="cursor-pointer"
              onClick={() => router.push(`/collaborators/${row.original.nationalId}`)}
            >
              {row.getVisibleCells().map((cell) => (
                <TableCell
                  key={cell.id}
                  onClick={cell.column.id === 'actions' ? (e) => e.stopPropagation() : undefined}
                >
                  {flexRender(cell.column.columnDef.cell, cell.getContext())}
                </TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>

      <ConfirmDialog
        open={!!deleteTarget}
        onOpenChange={(open) => !open && setDeleteTarget(null)}
        title={`Delete ${deleteTarget?.fullName}?`}
        description="This will permanently remove the collaborator and all associated performance reviews. This action cannot be undone."
        confirmLabel="Delete"
        isLoading={isDeleting}
        onConfirm={handleConfirmDelete}
      />
    </>
  )
}
