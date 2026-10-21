# Architecture Decision Records

Decisiones tomadas durante la reescritura. El formato es una versión breve de ADR:
contexto, decisión, consecuencias y alternativas.

| ADR | Decisión | Estado |
|---|---|---|
| [001](ADR-001-reescritura-limpia.md) | Reescritura limpia en lugar de reparar el legacy | Aceptado |
| [002](ADR-002-contextos-y-dominio-puro.md) | Contextos delimitados y dominio puro | Aceptado |
| [003](ADR-003-ids-uuid-tipados.md) | Identificadores UUID tipados | Aceptado |
| [004](ADR-004-in-memory-primero.md) | Persistencia in-memory primero, JPA después | Temporal |
| [005](ADR-005-sanciones-por-eventos.md) | Sanciones por evento de dominio | Aceptado |
| [006](ADR-006-sin-contadores-de-stock.md) | Sin contadores de stock en el catálogo | Aceptado |
| [007](ADR-007-auth-fuera-del-corte-1.md) | Autenticación fuera del corte 1; login de personal y administradores | Temporal |
| [008](ADR-008-reservas-fuera-de-alcance.md) | Reservas fuera del alcance del corte 1 | Aceptado |

Cuando una decisión temporal se revierta (por ejemplo, al entrar JPA o JWT), actualiza el
ADR con una sección “Revisión” en lugar de borrarlo.
