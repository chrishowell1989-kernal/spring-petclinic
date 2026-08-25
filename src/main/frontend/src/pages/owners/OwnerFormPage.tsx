import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import Typography from '@mui/material/Typography'
import Button from '@mui/material/Button'
import Stack from '@mui/material/Stack'
import { useTranslation } from 'react-i18next'
import { ownersApi } from '../../api/owners'
import { ApiError } from '../../api/client'
import type { OwnerRequest } from '../../api/types'
import FormTextField from '../../components/common/FormTextField'

const emptyOwner: OwnerRequest = {
  firstName: '',
  lastName: '',
  address: '',
  city: '',
  telephone: '',
}

export default function OwnerFormPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { ownerId } = useParams<{ ownerId: string }>()
  const isEdit = Boolean(ownerId)

  const [owner, setOwner] = useState<OwnerRequest>(emptyOwner)
  const [apiError, setApiError] = useState<ApiError | null>(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (ownerId) {
      ownersApi.get(Number(ownerId)).then(setOwner)
    }
  }, [ownerId])

  const setField = (field: keyof OwnerRequest) => (value: string) =>
    setOwner((prev) => ({ ...prev, [field]: value }))

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    setSubmitting(true)
    setApiError(null)
    try {
      const saved = isEdit
        ? await ownersApi.update(Number(ownerId), owner)
        : await ownersApi.create(owner)
      navigate(`/owners/${saved.id}`, {
        state: { message: t(isEdit ? 'owners.ownerSaved' : 'owners.ownerAdded') },
      })
    } catch (err) {
      if (err instanceof ApiError) {
        setApiError(err)
      } else {
        throw err
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div>
      <Typography variant="h4" component="h1" gutterBottom>
        {t(isEdit ? 'ownerForm.editTitle' : 'ownerForm.newTitle')}
      </Typography>
      <form onSubmit={handleSubmit}>
        <FormTextField
          label={t('common.firstName')}
          value={owner.firstName}
          onChange={(e) => setField('firstName')(e.target.value)}
          errorMessage={apiError?.fieldError('firstName')}
          required
        />
        <FormTextField
          label={t('common.lastName')}
          value={owner.lastName}
          onChange={(e) => setField('lastName')(e.target.value)}
          errorMessage={apiError?.fieldError('lastName')}
          required
        />
        <FormTextField
          label={t('common.address')}
          value={owner.address}
          onChange={(e) => setField('address')(e.target.value)}
          errorMessage={apiError?.fieldError('address')}
          required
        />
        <FormTextField
          label={t('common.city')}
          value={owner.city}
          onChange={(e) => setField('city')(e.target.value)}
          errorMessage={apiError?.fieldError('city')}
          required
        />
        <FormTextField
          label={t('common.telephone')}
          value={owner.telephone}
          onChange={(e) => setField('telephone')(e.target.value)}
          errorMessage={apiError?.fieldError('telephone')}
          required
        />
        <Stack direction="row" spacing={2} sx={{ mt: 2 }}>
          <Button type="submit" variant="contained" disabled={submitting}>
            {t(isEdit ? 'ownerForm.submitEdit' : 'ownerForm.submitNew')}
          </Button>
        </Stack>
      </form>
    </div>
  )
}
