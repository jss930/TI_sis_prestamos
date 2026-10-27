import type { CatalogoItem } from '../types'

const imagen = (url: string) => `${url}?auto=format&fit=crop&w=800&q=80`

const imagenesPorTitulo: Record<string, string> = {
  'gafas de realidad virtual': imagen('https://images.unsplash.com/photo-1622979135225-d2ba269cf1ac'),
  'cámara web hd': imagen('https://images.unsplash.com/photo-1587825140708-dfaf72ae4b04'),
  'micrófono usb': imagen('https://images.unsplash.com/photo-1590602847861-f357a9332bbc'),
  'pulsera tecnológica': imagen('https://images.unsplash.com/photo-1575311373937-040b8e1fd6b0'),
  'laptop lenovo thinkpad t14': imagen('https://images.unsplash.com/photo-1496181133206-80ce9b88a853'),
  'laptop universitaria': imagen('https://images.unsplash.com/photo-1496181133206-80ce9b88a853'),
  'teclado usb': imagen('https://images.unsplash.com/photo-1587829741301-dc798b83add3'),
  'mouse usb': imagen('https://images.unsplash.com/photo-1527814050087-3793815479db'),
  'adaptador hdmi': imagen('https://images.unsplash.com/photo-1625842268584-8f3296236761'),
  'memoria usb 64 gb': 'https://images.unsplash.com/photo-1702141556838-ab291da11618?auto=format&fit=crop&w=800&q=80',
  'cargador universal': imagen('https://images.unsplash.com/photo-1583863788434-e58a36330cf0'),
  'puntero láser': imagen('https://images.unsplash.com/photo-1531058020387-3be344556be6'),
  'proyector multimedia': imagen('https://images.unsplash.com/photo-1535016120720-40c646be5580'),
  'trípode para cámara': imagen('https://images.unsplash.com/photo-1516035069371-29a1b244cc32'),
  auriculares: imagen('https://images.unsplash.com/photo-1505740420928-5e560c06d30e'),
  'clean code': imagen('https://images.unsplash.com/photo-1498050108023-c5249f4df085'),
  'clean architecture': 'https://amindfulcoder.com/assets/img/posts/fundamentals2.webp',
  'the c programming language': 'https://habrastorage.org/getpro/habr/upload_files/91b/d7b/6c4/91bd7b6c41ac1c8d0fbfbc0c1c83ca7c.png',
  'the c++ programming language': 'https://babak.uk/images/2025/ddia_book_laptop.png',
  'mesa de tenis de mesa': imagen('https://images.unsplash.com/photo-1559077184-562609f0a10b'),
  'pelota de vóley': imagen('https://images.unsplash.com/photo-1612872087720-bb876e2e67d1'),
  'pelota de fútbol': 'https://www.publicdomainpictures.net/pictures/640000/velka/football-club-football-team-sport.png',
  'pelota de básquet': imagen('https://images.unsplash.com/photo-1650566924908-db62de4863af'),
}

export function imagenCatalogo(item: CatalogoItem): string {
  const titulo = item.titulo.toLocaleLowerCase('es')
  return imagenesPorTitulo[titulo]
    ?? (item.tipo === 'LIBRO'
      ? 'https://covers.openlibrary.org/b/id/10521270-L.jpg'
      : 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=800&q=80')
}
