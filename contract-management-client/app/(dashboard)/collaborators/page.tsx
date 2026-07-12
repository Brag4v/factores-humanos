import { Suspense } from 'react'
import type { Metadata } from 'next'
import { CollaboratorsView } from '@/components/collaborators/CollaboratorsView'

export const metadata: Metadata = {
  title: 'Collaborators',
  description: 'Browse, search, and filter collaborators.',
}

export default function CollaboratorsPage() {
  return (
    <Suspense>
      <CollaboratorsView />
    </Suspense>
  )
}
