import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import Typography from '@mui/material/Typography'
import Button from '@mui/material/Button'
import Stack from '@mui/material/Stack'
import { DatePicker } from '@mui/x-date-pickers/DatePicker'
import dayjs from 'dayjs'
import type { Dayjs } from 'dayjs'
import { useTranslation } from 'react-i18next'
import { ownersApi } from '../../api/owners'
import { petsApi } from '../../api/pets'
import { petTypesApi } from '../../api/petTypes'
import { ApiError } from '../../api/client'
import type { PetTypeResponse } from '../../api/types'
import FormTextField from '../../components/common/FormTextField'
import FormSelectField from '../../components/common/FormSelectField'

export default function PetFormPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { ownerId, petId } = useParams<{ ownerId: string; petId?: string }>()
  const isEdit = Boolean(petId)

  const [ownerName, setOwnerName] = useState('')
  const [name, setName] = useState('')
  const [birthDate, setBirthDate] = useState<Dayjs | null>(null)
  const [typeId, setTypeId] = useState('')
  const [petTypes, setPetTypes] = useState<PetTypeResponse[]>([])
  const [apiError, setApiError] = useState<ApiError | null>(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (!ownerId) return
    ownersApi.get(Number(ownerId)).then((owner) => {
      setOwnerName(`${owner.firstName} ${owner.lastName}`)
      if (petId) {
        const pet = owner.pets.find((p) => p.id === Number(petId))
        if (pet) {
          setName(pet.name)
          setBirthDate(dayjs(pet.birthDate))
          setTypeId(String(pet.type.id))
        }
      }
    })
    petTypesApi.list().then(setPetTypes)
  }, [ownerId, petId])

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!ownerId) return
    setSubmitting(true)
    setApiError(null)
    const request = {
      name,
      birthDate: birthDate ? birthDate.format('YYYY-MM-DD') : '',
      typeId: Number(typeId),
    }
    try {
      if (isEdit) {
        await petsApi.update(Number(ownerId), Number(petId), request)
      } else {
        await petsApi.create(Number(ownerId), request)
      }
      navigate(`/owners/${ownerId}`, { state: { message: t('petForm.petSaved') } })
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
        {t(isEdit ? 'petForm.editTitle' : 'petForm.newTitle')}
      </Typography>
      <Typography variant="subtitle1" gutterBottom>
        {t('common.owner')}: {ownerName}
      </Typography>
      <form onSubmit={handleSubmit}>
        <FormTextField
          label={t('common.name')}
          value={name}
          onChange={(e) => setName(e.target.value)}
          errorMessage={apiError?.fieldError('name')}
          required
        />
        <DatePicker
          label={t('common.birthDate')}
          value={birthDate}
          onChange={setBirthDate}
          format="YYYY-MM-DD"
          slotProps={{
            textField: {
              fullWidth: true,
              margin: 'normal',
              error: Boolean(apiError?.fieldError('birthDate')),
              helperText: apiError?.fieldError('birthDate'),
            },
          }}
        />
        <FormSelectField
          label={t('petForm.typeLabel')}
          name="typeId"
          value={typeId}
          onChange={setTypeId}
          options={petTypes.map((pt) => ({ value: String(pt.id), label: pt.name }))}
          errorMessage={apiError?.fieldError('typeId')}
        />
        <Stack direction="row" spacing={2} sx={{ mt: 2 }}>
          <Button type="submit" variant="contained" disabled={submitting}>
            {t(isEdit ? 'petForm.submitEdit' : 'petForm.submitNew')}
          </Button>
        </Stack>
      </form>
    </div>
  )
}
