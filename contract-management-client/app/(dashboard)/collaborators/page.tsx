import { Suspense } from 'react'
import { CollaboratorsView } from '@/components/collaborators/CollaboratorsView'

export default function CollaboratorsPage() {
  return (
    <Suspense>
      <CollaboratorsView />
    </Suspense>
  )
}
