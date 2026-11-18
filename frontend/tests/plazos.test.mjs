import { readFile } from 'node:fs/promises'
import assert from 'node:assert/strict'
import test from 'node:test'
import ts from 'typescript'

const source = await readFile(new URL('../src/utils/plazos.ts', import.meta.url), 'utf8')
const { outputText } = ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.ESNext } })
const { validarPlazo, valorLima, instanteLima, finMaximo, permiteDomicilio, siguienteInicioReserva } =
  await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`)

test('matriz de domicilio para los nueve pares de perfil y bien', () => {
  for (const perfil of ['ALUMNO', 'DOCENTE', 'ADMINISTRATIVO']) {
    for (const tipo of ['LIBRO', 'EQUIPO', 'OTRO']) {
      const permitido = tipo === 'LIBRO' || (tipo === 'EQUIPO' && perfil !== 'ALUMNO')
      assert.equal(permiteDomicilio(perfil, tipo), permitido)
      if (!permitido) {
        assert.throws(() => validarPlazo('2026-11-17T07:00', '2026-11-17T08:00', 'DOMICILIO', perfil, tipo))
        continue
      }
      const dia = perfil === 'DOCENTE' && tipo === 'LIBRO' ? '22' : '20'
      validarPlazo('2026-11-17T07:00', `2026-11-${dia}T07:00`, 'DOMICILIO', perfil, tipo)
      assert.throws(() => validarPlazo('2026-11-17T07:00', `2026-11-${dia}T07:01`, 'DOMICILIO', perfil, tipo))
      assert.equal(finMaximo('2026-11-17T07:00', 'DOMICILIO', perfil, tipo), `2026-11-${dia}T07:00`)
    }
  }
})

test('campus siempre mismo día entre 07 y 20, de una a trece horas', () => {
  for (const perfil of ['ALUMNO', 'DOCENTE', 'ADMINISTRATIVO']) {
    for (const tipo of ['LIBRO', 'EQUIPO', 'OTRO']) {
      validarPlazo('2026-11-17T07:00', '2026-11-17T20:00', 'EN_CAMPUS', perfil, tipo)
      validarPlazo('2026-11-17T19:00', '2026-11-17T20:00', 'EN_CAMPUS', perfil, tipo)
      for (const [inicio, fin] of [['07:00', '07:59'], ['06:59', '08:00'], ['08:00', '20:01']]) {
        assert.throws(() => validarPlazo(`2026-11-17T${inicio}`, `2026-11-17T${fin}`, 'EN_CAMPUS', perfil, tipo))
      }
      assert.throws(() => validarPlazo('2026-11-17T19:00', '2026-11-18T07:00', 'EN_CAMPUS', perfil, tipo))
    }
  }
})

test('conversión explícita de Lima e inicio sugerido fuera de jornada', () => {
  assert.equal(valorLima(new Date('2026-11-17T12:00:00Z')), '2026-11-17T07:00')
  assert.equal(instanteLima('2026-11-17T20:00').toISOString(), '2026-11-18T01:00:00.000Z')
  assert.equal(siguienteInicioReserva(new Date('2026-11-18T00:00:00Z')), '2026-11-18T07:00')
})

test('presentación de fechas también es independiente de la zona del navegador', async () => {
  const source = await readFile(new URL('../src/utils/format.ts', import.meta.url), 'utf8')
  const { outputText } = ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.ESNext } })
  const { fechaCorta, fechaHoraLima } = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`)
  const anterior = process.env.TZ
  process.env.TZ = 'Asia/Tokyo'
  try {
    assert.match(fechaHoraLima('2026-11-17T15:00:00Z'), /17\/11\/2026.*10:00/)
    assert.match(fechaCorta('2026-11-17'), /17.*2026/)
    assert.equal(fechaHoraLima(null), '—')
  } finally {
    if (anterior) process.env.TZ = anterior
    else delete process.env.TZ
  }
})

test('menú sintetizado conserva permisos y separa la actividad personal', async () => {
  const source = await readFile(new URL('../src/utils/navegacion.ts', import.meta.url), 'utf8')
  const { outputText } = ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.ESNext } })
  const { opcionesNavegacion, puedeVerPersonas } = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`)
  assert.deepEqual(opcionesNavegacion([], true, false), ['catalogo', 'actividad'])
  assert.deepEqual(opcionesNavegacion(['ADMINISTRAR_SISTEMA'], false, false), ['catalogo'])
  assert.deepEqual(opcionesNavegacion(['ADMINISTRAR_SISTEMA'], true, true), ['catalogo'])
  const admin = opcionesNavegacion(['ADMINISTRAR_SISTEMA'], true, false)
  assert.ok(admin.includes('historial') && admin.includes('reservas') && admin.includes('actividad'))
  assert.ok(!admin.includes('prestamos')) // El permiso administrativo no concede ventanilla.
  assert.ok(puedeVerPersonas(['GESTIONAR_SANCIONES']))
  assert.ok(opcionesNavegacion(['REGISTRAR_DEVOLUCION'], true, false).includes('prestamos'))
  assert.ok(!admin.includes('administracion') && !admin.includes('mis-prestamos'))
})

test('períodos comprensibles y acumulación de páginas sin duplicados', async () => {
  const source = await readFile(new URL('../src/utils/reportes.ts', import.meta.url), 'utf8')
  const { outputText } = ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2020 } })
  const { descripcionPeriodo, unirRegistros } = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`)
  assert.equal(descripcionPeriodo(null, null), 'Todo el historial disponible')
  assert.equal(descripcionPeriodo('2026-11-01', '2026-11-17'), 'Del 01/11/2026 al 17/11/2026 (incluidos)')
  assert.equal(descripcionPeriodo(null, '2026-11-17'), 'Hasta el 17/11/2026 (incluido)')
  assert.deepEqual(unirRegistros([{ id: 'a' }, { id: 'b' }], [{ id: 'b' }, { id: 'c' }]), [{ id: 'a' }, { id: 'b' }, { id: 'c' }])
})
