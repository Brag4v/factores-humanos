'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { toast } from 'sonner'
import { StepIndicator } from '@/components/shared/StepIndicator'
import { Step1CollaboratorInfo } from './steps/Step1CollaboratorInfo'
import { Step2ContractDetails } from './steps/Step2ContractDetails'
import { Step3Review } from './steps/Step3Review'
import { useCreateCollaborator } from '@/lib/hooks/useCollaborators'
import { useCreateContract } from '@/lib/hooks/useContracts'
import { apiClient } from '@/lib/api/client'
import type { CollaboratorFormValues } from '@/lib/schemas/collaborator.schema'
import type { ContractFormValues } from '@/lib/schemas/contract.schema'

const STEPS = [
  { label: 'Collaborator Info' },
  { label: 'Contract Details' },
  { label: 'Review' },
]

export function NewCollaboratorFlow() {
  const router = useRouter()
  const [currentStep, setCurrentStep] = useState(0)
  const [collaboratorData, setCollaboratorData] = useState<CollaboratorFormValues | null>(null)
  const [contractData, setContractData] = useState<ContractFormValues | null>(null)
  const [contractFile, setContractFile] = useState<File | null>(null)

  const createCollaborator = useCreateCollaborator()
  const createContract = useCreateContract()

  const isSubmitting = createCollaborator.isPending || createContract.isPending

  function handleStep1Next(data: CollaboratorFormValues) {
    setCollaboratorData(data)
    setCurrentStep(1)
  }

  async function handleStep1Skip(data: CollaboratorFormValues) {
    setCollaboratorData(data)
    await submit(data, null)
  }

  function handleStep2Next(data: ContractFormValues, file: File | null) {
    setContractData(data)
    setContractFile(file)
    setCurrentStep(2)
  }

  function handleStep2Skip() {
    setContractData(null)
    setContractFile(null)
    setCurrentStep(2)
  }

  function handleBack() {
    setCurrentStep((s) => Math.max(0, s - 1))
  }

  async function handleConfirm() {
    if (!collaboratorData) return
    await submit(collaboratorData, contractData)
  }

  async function submit(collab: CollaboratorFormValues, contract: ContractFormValues | null) {
    let nationalId: string

    try {
      const created = await createCollaborator.mutateAsync({
        nationalId: collab.nationalId,
        firstName: collab.firstName,
        lastName: collab.lastName,
        email: collab.email,
        phone: collab.phone || undefined,
        department: collab.department || undefined,
        position: collab.position,
        status: collab.status,
        hireDate: collab.hireDate,
      })
      nationalId = created.nationalId
    } catch (err) {
      toast.error((err as Error)?.message ?? 'Failed to create collaborator. Please try again.')
      return
    }

    if (contract) {
      let contractId: string
      try {
        const created = await createContract.mutateAsync({
          collaboratorId: nationalId,
          contractType: contract.contractType,
          startDate: contract.startDate,
          endDate: contract.endDate,
          salary: contract.salary || undefined,
          currency: contract.currency,
          noticePeriodDays: contract.noticePeriodDays,
          autoRenewal: contract.autoRenewal,
          termsAndConditions: contract.termsAndConditions || undefined,
        })
        contractId = created.id
      } catch {
        toast.warning('Collaborator created, but the contract could not be saved. Add it from the detail page.')
        router.push(`/collaborators/${nationalId}`)
        return
      }

      if (contractFile) {
        try {
          const fd = new FormData()
          fd.append('file', contractFile)
          await apiClient.post(`/api/v1/contracts/${contractId}/documents`, fd, {
            headers: { 'Content-Type': 'multipart/form-data' },
          })
          toast.success('Collaborator, contract and document created successfully.')
        } catch {
          toast.warning('Contract created, but document upload failed. Upload it from the contract detail page.')
        }
      } else {
        toast.success('Collaborator and contract created successfully.')
      }
    } else {
      toast.success('Collaborator created successfully.')
    }

    router.push(`/collaborators/${nationalId}`)
  }

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <StepIndicator steps={STEPS} currentStep={currentStep} />

      {currentStep === 0 && (
        <Step1CollaboratorInfo
          defaultValues={collaboratorData ?? undefined}
          onNext={handleStep1Next}
          onSkip={handleStep1Skip}
          isSubmitting={isSubmitting}
        />
      )}

      {currentStep === 1 && (
        <Step2ContractDetails
          defaultValues={contractData ?? undefined}
          defaultFile={contractFile}
          onNext={handleStep2Next}
          onBack={handleBack}
          onSkip={handleStep2Skip}
        />
      )}

      {currentStep === 2 && collaboratorData && (
        <Step3Review
          collaboratorData={collaboratorData}
          contractData={contractData}
          onBack={handleBack}
          onConfirm={handleConfirm}
          isSubmitting={isSubmitting}
        />
      )}
    </div>
  )
}
