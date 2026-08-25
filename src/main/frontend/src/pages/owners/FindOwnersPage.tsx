import { useState } from 'react'
import { useNavigate, Link as RouterLink } from 'react-router-dom'
import Typography from '@mui/material/Typography'
import Stack from '@mui/material/Stack'
import Button from '@mui/material/Button'
import { useTranslation } from 'react-i18next'
import FormTextField from '../../components/common/FormTextField'

export default function FindOwnersPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [lastName, setLastName] = useState('')

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault()
    navigate(`/owners?lastName=${encodeURIComponent(lastName)}`)
  }

  return (
    <div>
      <Typography variant="h4" component="h1" gutterBottom>
        {t('owners.findTitle')}
      </Typography>
      <form onSubmit={handleSubmit}>
        <FormTextField
          label={t('owners.lastNameLabel')}
          name="lastName"
          value={lastName}
          onChange={(e) => setLastName(e.target.value)}
          sx={{ maxWidth: 320 }}
        />
        <Stack direction="row" spacing={2} sx={{ mt: 2 }}>
          <Button type="submit" variant="contained">
            {t('owners.searchButton')}
          </Button>
          <Button component={RouterLink} to="/owners/new" variant="outlined">
            {t('owners.addOwnerButton')}
          </Button>
        </Stack>
      </form>
    </div>
  )
}
