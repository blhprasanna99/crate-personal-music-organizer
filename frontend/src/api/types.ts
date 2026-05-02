export type Track = {
  id: number
  filePath: string
  title: string | null
  artist: string | null
  album: string | null
  durationMs: number | null
  createdAt: string
}

export type Crate = {
  id: number
  name: string
  description: string | null
  createdAt: string
}

export type SmartCrate = {
  id: number
  name: string
  description: string | null
  criteria: Filter[]
  createdAt: string
}

export type Filter = { op: string; value: unknown }

export type ImportSummary = {
  scanned: number
  imported: number
  skipped: number
  failed: number
  errors: { path: string; reason: string }[]
}
