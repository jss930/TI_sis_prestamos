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
