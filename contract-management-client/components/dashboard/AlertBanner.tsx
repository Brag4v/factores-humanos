'use client'

import { useState } from 'react'
import Link from 'next/link'
import { AlertTriangle, X } from 'lucide-react'
import type { ExpiringContractResponse } from '@/lib/types'

interface AlertBannerProps {
  contracts: ExpiringContractResponse[]
}

export function AlertBanner({ contracts }: AlertBannerProps) {
  const [dismissed, setDismissed] = useState(false)

  if (dismissed || contracts.length === 0) return null

  const preview = contracts.slice(0, 3)
  const remaining = contracts.length - preview.length

  return (
    <div className="flex items-start gap-3 rounded-lg border border-red-200 bg-red-50 p-4">
      <AlertTriangle className="mt-0.5 h-5 w-5 flex-shrink-0 text-red-600" />

      <div className="flex-1 min-w-0">
        <p className="font-semibold text-red-800">
          {contracts.length} contract{contracts.length > 1 ? 's' : ''} require immediate attention
        </p>
        <p className="mt-1 text-sm text-red-700">
          Expiring in 7 days or less:{' '}
          {preview.map((c, i) => (
            <span key={c.id}>
              <Link href={`/contracts/${c.id}`} className="font-medium underline underline-offset-2 hover:text-red-900">
                {c.contractNumber}
              </Link>
              {i < preview.length - 1 || remaining > 0 ? ', ' : ''}
            </span>
          ))}
          {remaining > 0 && <span>and {remaining} more</span>}
        </p>
        <Link
          href="/contracts?status=ACTIVE"
          className="mt-2 inline-block text-sm font-medium text-red-800 underline underline-offset-2 hover:text-red-900"
        >
          View all expiring contracts →
        </Link>
      </div>

      <button
        onClick={() => setDismissed(true)}
        className="flex-shrink-0 rounded p-0.5 text-red-400 hover:bg-red-100 hover:text-red-600 transition-colors"
        aria-label="Dismiss alert"
      >
        <X className="h-4 w-4" />
      </button>
    </div>
  )
}
