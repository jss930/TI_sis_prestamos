# ADR-012: Un principal y un suplente por área

Estado: aceptado.

## Contexto

Las responsabilidades permitían asignar un número ilimitado de personas a una categoría. Se requiere identificar un principal y admitir como máximo un suplente en cada área.

## Decisión

Cada asignación relaciona persona, área y cargo (`PRINCIPAL` o `SUPLENTE`). Una persona puede ocupar un único cargo en un área, y cada cargo admite una única persona. Un mismo usuario puede tener cargos distintos en áreas diferentes. Un suplente puede permanecer asignado si se retira al principal; no hay promoción automática ni reemplazos implícitos.

Los cargos se guardan en `cargos_responsables`, con clave primaria persona/área y unicidad área/cargo. Este mapa es la fuente de las categorías autorizadas; el CSV anterior se mantiene sincronizado solo para compatibilidad con los datos existentes. La política del dominio comprueba cargos ocupados. Todas las operaciones administrativas de asignación usan el mismo bloqueo transaccional antes de cargar las responsabilidades para evitar que dos confirmaciones ocupen el mismo cargo. La restricción de base de datos impide también duplicados.

La pantalla conserva la confirmación explícita y la limpieza del formulario después de guardar. Muestra los ocupantes actuales, permite seleccionar cargo y deshabilita los cargos ocupados por otra persona. Para reemplazar a alguien es necesario retirar o cambiar su asignación y confirmar primero. El backend comprueba las mismas reglas aunque el cliente tenga datos anteriores.

La migración V11 conserva las asignaciones: ordena a sus ocupantes por la asignación histórica más antigua, con fecha de alta y UUID como desempate, y asigna principal y suplente respectivamente. Si una base tuviera más de dos personas por área, la migración fallaría con sus restricciones y revertiría sin descartar asignaciones; debe resolverse esa situación antes de aplicar V11.

## Consecuencias

Principal y suplente tienen las mismas facultades operativas para el área, conservando la validación de cuenta y la capacitación técnica. El permiso de administración general no cambia. El historial nuevo registra el cargo y los cambios de principal a suplente o viceversa; las entradas anteriores conservan su información original.
