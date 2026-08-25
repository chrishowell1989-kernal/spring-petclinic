import { useEffect, useState } from 'react'
import { useParams, Link as RouterLink } from 'react-router-dom'
import Typography from '@mui/material/Typography'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableRow from '@mui/material/TableRow'
import TableCell from '@mui/material/TableCell'
import Stack from '@mui/material/Stack'
import Button from '@mui/material/Button'
import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import { useTranslation } from 'react-i18next'
import { ownersApi } from '../../api/owners'
import type { OwnerResponse } from '../../api/types'
import FlashSnackbar from '../../components/common/FlashSnackbar'

export default function OwnerDetailsPage() {
  const { t } = useTranslation()
  const { ownerId } = useParams<{ ownerId: string }>()
  const [owner, setOwner] = useState<OwnerResponse | null>(null)

  useEffect(() => {
    if (!ownerId) return
    ownersApi.get(Number(ownerId)).then(setOwner)
  }, [ownerId])

  if (!owner) {
    return <Typography>{t('common.loading')}</Typography>
  }

  return (
    <div>
      <FlashSnackbar />
      <Typography variant="h4" component="h1" gutterBottom>
        {t('owners.detailsTitle')}
      </Typography>
      <Table sx={{ maxWidth: 480 }}>
        <TableBody>
          <TableRow>
            <TableCell component="th">{t('common.name')}</TableCell>
            <TableCell>
              {owner.firstName} {owner.lastName}
            </TableCell>
          </TableRow>
          <TableRow>
            <TableCell component="th">{t('owners.tableAddress')}</TableCell>
            <TableCell>{owner.address}</TableCell>
          </TableRow>
          <TableRow>
            <TableCell component="th">{t('common.city')}</TableCell>
            <TableCell>{owner.city}</TableCell>
          </TableRow>
          <TableRow>
            <TableCell component="th">{t('common.telephone')}</TableCell>
            <TableCell>{owner.telephone}</TableCell>
          </TableRow>
        </TableBody>
      </Table>

      <Stack direction="row" spacing={2} sx={{ my: 2 }}>
        <Button component={RouterLink} to={`/owners/${owner.id}/edit`} variant="outlined">
          {t('owners.editOwnerButton')}
        </Button>
        <Button component={RouterLink} to={`/owners/${owner.id}/pets/new`} variant="outlined">
          {t('owners.addNewPetButton')}
        </Button>
      </Stack>

      <Typography variant="h5" component="h2" gutterBottom>
        {t('owners.petsAndVisits')}
      </Typography>
      <Stack spacing={2}>
        {owner.pets.map((pet) => (
          <Card key={pet.id}>
            <CardContent>
              <Typography variant="h6">{pet.name}</Typography>
              <Typography variant="body2" color="text.secondary">
                {t('common.birthDate')}: {pet.birthDate} &middot; {t('common.type')}: {pet.type.name}
              </Typography>

              {pet.visits.length > 0 ? (
                <Table size="small" sx={{ mt: 1 }}>
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
                <Typography variant="body2" sx={{ mt: 1 }}>
                  {t('owners.noVisits')}
                </Typography>
              )}

              <Stack direction="row" spacing={2} sx={{ mt: 2 }}>
                <Button
                  size="small"
                  component={RouterLink}
                  to={`/owners/${owner.id}/pets/${pet.id}/edit`}
                >
                  {t('owners.editPetButton')}
                </Button>
                <Button
                  size="small"
                  component={RouterLink}
                  to={`/owners/${owner.id}/pets/${pet.id}/visits/new`}
                >
                  {t('owners.addVisitButton')}
                </Button>
              </Stack>
            </CardContent>
          </Card>
        ))}
      </Stack>
    </div>
  )
}
