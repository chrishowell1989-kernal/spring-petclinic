import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import Typography from '@mui/material/Typography'
import Button from '@mui/material/Button'
import Stack from '@mui/material/Stack'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableRow from '@mui/material/TableRow'
import TableCell from '@mui/material/TableCell'
import { DatePicker } from '@mui/x-date-pickers/DatePicker'
import dayjs from 'dayjs'
import type { Dayjs } from 'dayjs'
import { useTranslation } from 'react-i18next'
import { ownersApi } from '../../api/owners'
import { visitsApi } from '../../api/visits'
import { ApiError } from '../../api/client'
import type { PetResponse } from '../../api/types'
import FormTextField from '../../components/common/FormTextField'

export default function VisitFormPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { ownerId, petId } = useParams<{ ownerId: string; petId: string }>()

  const [pet, setPet] = useState<PetResponse | null>(null)
  const [date, setDate] = useState<Dayjs | null>(dayjs().add(1, 'day'))
  const [description, setDescription] = useState('')
  const [apiError, setApiError] = useState<ApiError | null>(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (!ownerId || !petId) return
    ownersApi.get(Number(ownerId)).then((owner) => {
      const found = owner.pets.find((p) => p.id === Number(petId))
      setPet(found ?? null)
    })
  }, [ownerId, petId])

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!ownerId || !petId) return
    setSubmitting(true)
    setApiError(null)
    try {
      await visitsApi.create(Number(ownerId), Number(petId), {
        date: date ? date.format('YYYY-MM-DD') : '',
        description,
      })
      navigate(`/owners/${ownerId}`, { state: { message: t('visitForm.visitAdded') } })
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

  if (!pet) {
    return <Typography>{t('common.loading')}</Typography>
  }

  return (
    <div>
      <Typography variant="h4" component="h1" gutterBottom>
        {t('visitForm.title')}
      </Typography>
      <Typography variant="subtitle1">
        {pet.name} &middot; {t('common.birthDate')}: {pet.birthDate} &middot; {t('common.type')}:{' '}
        {pet.type.name}
      </Typography>

      <Typography variant="h6" sx={{ mt: 3 }}>
        {t('owners.previousVisits')}
      </Typography>
      {pet.visits.length > 0 ? (
        <Table size="small">
          <TableBody>
            {pet.visits.map((visit) => (
              <TableRow key={visit.id}>
                <TableCell>{visit.date}</TableCell>
                <TableCell>{visit.description}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      ) : (
        <Typography variant="body2">{t('owners.noVisits')}</Typography>
      )}

      <form onSubmit={handleSubmit}>
        <DatePicker
          label={t('common.date')}
          value={date}
          onChange={setDate}
          format="YYYY-MM-DD"
          slotProps={{
            textField: {
              fullWidth: true,
              margin: 'normal',
              error: Boolean(apiError?.fieldError('date')),
              helperText: apiError?.fieldError('date'),
            },
          }}
        />
        <FormTextField
          label={t('common.description')}
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          errorMessage={apiError?.fieldError('description')}
          multiline
          rows={3}
          required
        />
        <Stack direction="row" spacing={2} sx={{ mt: 2 }}>
          <Button type="submit" variant="contained" disabled={submitting}>
            {t('visitForm.submit')}
          </Button>
        </Stack>
      </form>
    </div>
  )
}
