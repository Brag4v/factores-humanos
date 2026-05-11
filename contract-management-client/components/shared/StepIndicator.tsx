import { Check } from 'lucide-react'
import { cn } from '@/lib/utils'

interface Step {
  label: string
}

interface StepIndicatorProps {
  steps: Step[]
  currentStep: number
}

export function StepIndicator({ steps, currentStep }: StepIndicatorProps) {
  return (
    <nav aria-label="Progress">
      <ol className="flex items-center">
        {steps.map((step, index) => {
          const isCompleted = index < currentStep
          const isActive = index === currentStep
          const isLast = index === steps.length - 1

          return (
            <li key={step.label} className={cn('flex items-center', !isLast && 'flex-1')}>
              {/* Circle */}
              <div className="flex flex-col items-center gap-1">
                <div
                  className={cn(
                    'flex h-9 w-9 items-center justify-center rounded-full border-2 text-sm font-semibold transition-all',
                    isCompleted && 'border-indigo-600 bg-indigo-600 text-white',
                    isActive && 'border-indigo-600 bg-white text-indigo-600',
                    !isCompleted && !isActive && 'border-slate-200 bg-white text-slate-400'
                  )}
                >
                  {isCompleted ? <Check className="h-4 w-4" /> : <span>{index + 1}</span>}
                </div>
                <span
                  className={cn(
                    'whitespace-nowrap text-xs font-medium',
                    isActive || isCompleted ? 'text-indigo-600' : 'text-slate-400'
                  )}
                >
                  {step.label}
                </span>
              </div>

              {/* Connector line */}
              {!isLast && (
                <div
                  className={cn(
                    'mb-5 mx-3 h-0.5 flex-1 transition-colors',
                    isCompleted ? 'bg-indigo-600' : 'bg-slate-200'
                  )}
                />
              )}
            </li>
          )
        })}
      </ol>
    </nav>
  )
}
