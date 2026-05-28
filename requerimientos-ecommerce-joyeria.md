# Documento de Requerimientos - E-commerce de Joyería
## Sistema Monolítico - Single Vendor

**Versión:** 1.0  
**Fecha:** 04 de Febrero de 2026  
**Stack Tecnológico:** Java Spring Boot + React + PostgreSQL

---

## 1. VISIÓN GENERAL DEL PROYECTO

### 1.1 Descripción
Sistema e-commerce monolítico para venta de joyería en línea, operando únicamente en México, con gestión centralizada de inventario, procesamiento de pagos mediante Stripe y PayPal, y envíos a través de DHL y FedEx.

### 1.2 Objetivos del Sistema
- Facilitar la venta en línea de productos de joyería
- Gestionar inventario en tiempo real desde dashboard administrativo
- Procesar pagos seguros con múltiples métodos
- Permitir compras tanto a usuarios registrados como invitados
- Proveer experiencia de usuario responsive en todos los dispositivos

### 1.3 Alcance Inicial
- Catálogo: Máximo 1,000 productos
- Usuarios concurrentes: Hasta 500
- Operación: Solo México
- Moneda: Pesos mexicanos (MXN)
- Administradores: 1 usuario admin

---

## 2. REQUERIMIENTOS FUNCIONALES - BACKEND

### 2.1 Módulo de Autenticación y Autorización

#### 2.1.1 Registro de Usuarios
**RF-AUTH-001:** El sistema debe permitir registro con email/contraseña
- Validación de formato de email
- Password mínimo 8 caracteres (1 mayúscula, 1 minúscula, 1 número, 1 caracter especial)
- Envío automático de email de verificación
- Token de verificación con expiración de 24 horas
- La cuenta no estará activa hasta verificar el email

**RF-AUTH-002:** El sistema debe permitir registro con Google OAuth 2.0
- Integración con Google Sign-In
- Extracción automática de datos del perfil (nombre, email, foto)
- Creación automática de cuenta verificada

**RF-AUTH-003:** El sistema debe permitir registro con Facebook OAuth 2.0
- Integración con Facebook Login
- Extracción automática de datos del perfil
- Creación automática de cuenta verificada

#### 2.1.2 Inicio de Sesión
**RF-AUTH-004:** El sistema debe permitir login con email/contraseña
- Implementación de JWT (Access Token + Refresh Token)
- Access Token: expiración 15 minutos
- Refresh Token: expiración 7 días, almacenado en httpOnly cookie
- Validación de cuenta verificada antes de permitir login

**RF-AUTH-005:** El sistema debe permitir login con Google/Facebook
- Proceso de autenticación OAuth
- Generación de JWT tras validación exitosa

**RF-AUTH-006:** Límite de intentos de login fallidos
- Máximo 5 intentos fallidos consecutivos
- Bloqueo temporal de cuenta por 30 minutos tras 5 intentos
- Notificación por email de bloqueo de cuenta
- Opción de desbloqueo mediante recuperación de contraseña

**RF-AUTH-007:** Implementar CAPTCHA en login y registro
- Google reCAPTCHA v3 en formularios de registro
- reCAPTCHA v2 (checkbox) tras 3 intentos de login fallidos

#### 2.1.3 Recuperación de Contraseña
**RF-AUTH-008:** El sistema debe permitir recuperación de contraseña
- Solicitud mediante email registrado
- Envío de token de recuperación (válido 1 hora)
- Enlace único de restablecimiento
- Formulario de nueva contraseña
- Invalidación de todas las sesiones activas tras cambio

#### 2.1.4 Gestión de Tokens
**RF-AUTH-009:** Endpoint de refresh token
- Validación de refresh token desde cookie
- Generación de nuevo access token
- Rotación de refresh token (opcional pero recomendado)

**RF-AUTH-010:** Logout
- Invalidación de refresh token
- Eliminación de cookie httpOnly
- Registro de logout en log de auditoría

#### 2.1.5 Roles y Permisos
**RF-AUTH-011:** El sistema debe manejar 3 roles:
- **GUEST:** Usuario no autenticado (puede agregar al carrito, checkout)
- **USER:** Usuario registrado y verificado
- **ADMIN:** Administrador único del sistema

**RF-AUTH-012:** Control de acceso basado en roles (RBAC)
- Endpoints protegidos según rol
- Middleware de validación de permisos

### 2.2 Módulo de Gestión de Usuarios

#### 2.2.1 Perfil de Usuario
**RF-USER-001:** El usuario debe poder ver y editar su perfil
- Nombre completo
- Email (no editable si es el de autenticación)
- Teléfono
- Foto de perfil (opcional)

**RF-USER-002:** Gestión de direcciones de envío
- CRUD completo de direcciones
- Múltiples direcciones guardadas
- Marcar dirección como predeterminada
- Campos: nombre destinatario, calle, número exterior/interior, colonia, código postal, ciudad, estado, referencias

**RF-USER-003:** Lista de deseos (Wishlist)
- Agregar/eliminar productos
- Ver lista completa
- Notificación si producto en wishlist baja de precio (opcional v2)
- Mover producto de wishlist a carrito

**RF-USER-004:** Historial de órdenes
- Ver todas las órdenes realizadas
- Filtrar por estado (en proceso, pagada, enviada, entregada, cancelada)
- Ver detalle completo de cada orden
- Descargar resumen de orden (PDF)

#### 2.2.2 Checkout de Invitado
**RF-USER-005:** Compra sin registro
- Solicitar email obligatorio
- Enviar email de confirmación de compra
- Enlace único para rastreo de orden
- No se guarda historial después de completar la compra

### 2.3 Módulo de Productos

#### 2.3.1 Estructura de Producto
**RF-PROD-001:** Modelo de producto con los siguientes atributos:
- ID único (UUID)
- Nombre (max 200 caracteres)
- Descripción corta (max 500 caracteres)
- Descripción larga (texto enriquecido, max 5000 caracteres)
- SKU (único, alfanumérico)
- Precio base (decimal, 2 decimales)
- Precio con descuento (opcional)
- Categoría (relación con tabla categorías)
- Marca
- Material(es) (relación many-to-many)
- Garantía (en meses, entero)
- Stock total
- Estado (activo, inactivo, agotado)
- Fecha de creación
- Fecha de última actualización
- Peso (para cálculo de envío, en gramos)
- Destacado (boolean para productos en home)

#### 2.3.2 Variantes de Producto
**RF-PROD-002:** Sistema de variantes
- Un producto puede tener múltiples variantes
- Atributos de variante:
  - Talla (ej: 5, 6, 7 para anillos)
  - Color (ej: oro, plata, oro rosa)
  - SKU específico de variante
  - Stock específico
  - Precio diferencial (opcional, +/- del precio base)
- Control de stock independiente por variante

#### 2.3.3 Multimedia
**RF-PROD-003:** Gestión de imágenes de producto
- Máximo 10 imágenes por producto
- Formatos aceptados: JPG, PNG, WEBP
- Tamaño máximo por imagen: 5MB
- Orden de imágenes configurable
- Imagen principal obligatoria
- Almacenamiento en sistema de archivos del servidor (path en BD)
- Generación automática de thumbnails (200x200, 400x400)

**RF-PROD-004:** Gestión de videos de producto
- Máximo 2 videos por producto
- Formatos aceptados: MP4, WEBM
- Tamaño máximo por video: 50MB
- Almacenamiento en sistema de archivos
- Thumbnail automático del primer frame

#### 2.3.4 Categorías
**RF-PROD-005:** Sistema de categorías jerárquico
- Categorías padre e hijas (máximo 2 niveles)
- CRUD completo de categorías desde admin
- Atributos: nombre, slug, descripción, imagen, estado (activa/inactiva)
- Un producto pertenece a una sola categoría

#### 2.3.5 Reviews y Calificaciones
**RF-PROD-006:** Sistema de reseñas de clientes
- Solo usuarios con orden "entregada" del producto pueden dejar review
- Un review por usuario por producto
- Atributos:
  - Calificación (1-5 estrellas)
  - Título (max 100 caracteres)
  - Comentario (max 1000 caracteres)
  - Fecha de publicación
  - Estado (pendiente, aprobado, rechazado)
- Moderación desde admin dashboard
- Cálculo automático de calificación promedio del producto

**RF-PROD-007:** Validación de reviews
- Verificación de compra antes de permitir review
- Solo 1 review por usuario por producto
- Admin puede aprobar/rechazar/eliminar reviews

### 2.4 Módulo de Carrito de Compras

**RF-CART-001:** Carrito para usuarios autenticados
- Persistencia en base de datos
- Mantener carrito entre sesiones
- Agregar/actualizar cantidad/eliminar items
- Validación de stock disponible al agregar
- Actualización automática si stock cambia

**RF-CART-002:** Carrito para usuarios invitados
- Gestión del lado del cliente (localStorage)
- Validación de stock en backend al hacer checkout
- Conversión a carrito de usuario si se registra/loggea durante checkout

**RF-CART-003:** Validaciones de carrito
- Stock disponible antes de checkout
- Precio actualizado (en caso de cambios)
- Variantes correctas del producto
- Productos activos (no eliminados)

**RF-CART-004:** Aplicación de cupones
- Endpoint para validar código de cupón
- Tipos de descuento: porcentaje o monto fijo
- Validaciones:
  - Cupón activo
  - Fecha de vigencia
  - Uso máximo no excedido
  - Monto mínimo de compra cumplido (opcional)
- Cálculo de descuento aplicado

### 2.5 Módulo de Órdenes

#### 2.5.1 Creación de Orden
**RF-ORDER-001:** Proceso de checkout
- Validación de carrito (stock, precios, productos activos)
- Cálculo de subtotal
- Aplicación de cupón de descuento (si aplica)
- Selección de dirección de envío
- Cálculo de costo de envío (tarifa fija según zona/paquetería)
- Cálculo de total
- Selección de método de pago (Stripe o PayPal)

**RF-ORDER-002:** Generación de orden
- Número de orden único (ej: ORD-2026020401234)
- Estados: EN_PROCESO → PAGADA → ENVIADA → ENTREGADA / CANCELADA
- Captura completa de:
  - Items (snapshot del producto al momento de compra)
  - Precios al momento de compra
  - Dirección de envío completa
  - Método de pago utilizado
  - Cupón aplicado
  - Costo de envío
  - Subtotal, descuento, total
- Timestamp de creación

#### 2.5.2 Procesamiento de Pagos
**RF-ORDER-003:** Integración con Stripe
- Creación de Payment Intent
- Webhooks para estados de pago:
  - payment_intent.succeeded → actualizar orden a PAGADA
  - payment_intent.payment_failed → mantener en EN_PROCESO, notificar usuario
- Almacenamiento seguro de transaction ID
- No almacenar datos de tarjeta (PCI compliance)

**RF-ORDER-004:** Integración con PayPal
- Creación de orden en PayPal
- Captura de pago tras aprobación
- Webhooks de PayPal para confirmación
- Almacenamiento de transaction ID

**RF-ORDER-005:** Manejo de fallos de pago
- Reintentos automáticos (3 intentos máximo)
- Notificación al usuario de fallo
- Orden permanece en estado EN_PROCESO
- Permitir reintento manual de pago

#### 2.5.3 Gestión de Estados
**RF-ORDER-006:** Transiciones de estado
- EN_PROCESO → PAGADA (automático tras pago exitoso)
- PAGADA → ENVIADA (manual desde admin, requiere número de guía)
- ENVIADA → ENTREGADA (manual desde admin tras confirmación)
- EN_PROCESO/PAGADA → CANCELADA (usuario o admin)
- ENVIADA/ENTREGADA no pueden cancelarse

**RF-ORDER-007:** Cancelación de órdenes
- Usuario puede cancelar si estado = EN_PROCESO o PAGADA
- Si estado = PAGADA, iniciar proceso de reembolso
- Admin puede cancelar cualquier orden no enviada
- Registro de motivo de cancelación
- Restauración de stock de productos

**RF-ORDER-008:** Devoluciones
- Usuario solicita devolución desde su historial de órdenes
- Estados de devolución: SOLICITADA, EN_REVISION, APROBADA, RECHAZADA, COMPLETADA
- Admin ve solicitudes en dashboard
- Admin contacta por email (proceso manual)
- Admin aprueba/rechaza devolución
- Si se aprueba, se inicia proceso de reembolso
- Registro de motivo de devolución

#### 2.5.4 Notificaciones
**RF-ORDER-009:** Emails transaccionales
- Orden creada (estado EN_PROCESO)
- Pago confirmado (estado PAGADA)
- Orden enviada (estado ENVIADA) - incluir número de guía
- Orden entregada (estado ENTREGADA)
- Orden cancelada (estado CANCELADA) - incluir motivo
- Solicitud de devolución recibida
- Estado de devolución actualizado

**RF-ORDER-010:** Contenido de emails
- Número de orden
- Resumen de productos
- Total pagado
- Dirección de envío
- Método de pago
- Enlace para rastrear orden (usuarios invitados)
- Enlace a cuenta (usuarios registrados)

### 2.6 Módulo de Inventario

**RF-INV-001:** Control de stock en tiempo real
- Descuento automático de stock al confirmar pago
- Restauración de stock al cancelar orden
- Alertas de inventario bajo (configurable por producto)
- Stock reservado durante checkout (15 minutos timeout)

**RF-INV-002:** Ajustes manuales de inventario
- Admin puede ajustar stock manualmente
- Registro de movimientos de inventario:
  - Tipo: VENTA, CANCELACION, AJUSTE_MANUAL, DEVOLUCION
  - Cantidad
  - Usuario que realizó el cambio
  - Timestamp
  - Notas/motivo

**RF-INV-003:** Validaciones de stock
- No permitir cantidad negativa
- Verificar disponibilidad antes de agregar al carrito
- Verificar disponibilidad antes de confirmar pago
- Actualizar estado de producto a AGOTADO si stock = 0

### 2.7 Módulo de Envíos

**RF-SHIP-001:** Cálculo de costo de envío
- Tarifas fijas configurables por:
  - Zona/Estado de destino
  - Paquetería (DHL, FedEx)
  - Peso del paquete
- Tabla de configuración de tarifas en BD

**RF-SHIP-002:** Gestión de envíos
- Admin asigna paquetería al procesar orden
- Admin ingresa número de guía al marcar como ENVIADA
- Almacenamiento de:
  - Paquetería utilizada
  - Número de guía
  - Fecha de envío
  - Fecha estimada de entrega

**RF-SHIP-003:** Información de envío para el cliente
- Mostrar paquetería y número de guía
- Enlace directo a tracking de paquetería (DHL/FedEx)

### 2.8 Módulo de Búsqueda y Filtrado

**RF-SEARCH-001:** Búsqueda de productos
- Búsqueda por texto en:
  - Nombre del producto
  - Descripción
  - SKU
- Búsqueda por categoría específica
- Búsqueda combinada (texto + categoría)

**RF-SEARCH-002:** Filtros de productos
- Por rango de precio (min-max)
- Por material (checkbox múltiple)
- Por color (checkbox múltiple)
- Por marca (checkbox múltiple)
- Por calificación (mínimo de estrellas)
- Por disponibilidad (solo en stock)

**RF-SEARCH-003:** Ordenamiento de resultados
- Precio: menor a mayor
- Precio: mayor a menor
- Más vendidos
- Más nuevos (fecha de creación)
- Mejor calificados

**RF-SEARCH-004:** Paginación
- Resultados paginados (20 productos por página)
- Información de total de resultados
- Navegación entre páginas

### 2.9 Módulo de Recomendaciones

**RF-RECOM-001:** Productos relacionados
- Basado en categoría compartida
- Basado en material compartido
- Mostrar 4-6 productos relacionados por producto
- Excluir el producto actual
- Priorizar productos en stock

**RF-RECOM-002:** Productos recomendados (Home)
- Productos destacados (marcados por admin)
- Productos más vendidos (últimos 30 días)
- Productos nuevos (últimos 30 días)
- Secciones configurables desde admin

### 2.10 Módulo de Cupones y Promociones

**RF-COUPON-001:** CRUD de cupones desde admin
- Atributos:
  - Código único (alfanumérico, mayúsculas)
  - Tipo: porcentaje o monto fijo
  - Valor del descuento
  - Fecha inicio de vigencia
  - Fecha fin de vigencia
  - Usos máximos totales (opcional)
  - Usos máximos por usuario (opcional)
  - Monto mínimo de compra (opcional)
  - Aplicable a categorías específicas (opcional)
  - Estado: activo/inactivo
- Validaciones de unicidad de código

**RF-COUPON-002:** Aplicación de cupones
- Validación en tiempo real al ingresar código
- Mensajes de error claros:
  - Código inválido
  - Cupón expirado
  - Límite de usos excedido
  - Monto mínimo no alcanzado
- Cálculo automático de descuento
- Permitir quitar cupón aplicado

**RF-COUPON-003:** Seguimiento de uso
- Registro de cada uso de cupón
- Contador de usos totales
- Usos por usuario
- Reportes de cupones más utilizados

### 2.11 Módulo de Administración (Dashboard)

#### 2.11.1 Gestión de Productos
**RF-ADMIN-001:** CRUD completo de productos
- Crear producto con variantes
- Editar producto y variantes
- Subir/eliminar imágenes y videos
- Activar/desactivar productos
- Eliminar productos (soft delete)
- Vista de productos con stock bajo

**RF-ADMIN-002:** Gestión de categorías
- CRUD completo de categorías
- Gestión de jerarquía (categorías padre/hija)
- Reordenar categorías

**RF-ADMIN-003:** Gestión de inventario
- Ver stock actual por producto/variante
- Ajustar stock manualmente
- Ver historial de movimientos
- Exportar reporte de inventario

#### 2.11.2 Gestión de Órdenes
**RF-ADMIN-004:** Visualización de órdenes
- Lista de todas las órdenes
- Filtros por:
  - Estado
  - Rango de fechas
  - Método de pago
  - Búsqueda por número de orden o email de cliente
- Vista detallada de orden

**RF-ADMIN-005:** Procesamiento de órdenes
- Cambiar estado de orden
- Asignar paquetería y número de guía
- Marcar como enviada
- Marcar como entregada
- Cancelar orden con motivo
- Descargar comprobante de orden

**RF-ADMIN-006:** Gestión de devoluciones
- Ver solicitudes de devolución
- Estados: SOLICITADA, EN_REVISION, APROBADA, RECHAZADA, COMPLETADA
- Ver detalles de orden asociada
- Información de contacto del cliente
- Aprobar/rechazar devolución
- Registrar notas internas
- Marcar devolución como completada
- Iniciar proceso de reembolso (link a Stripe/PayPal)

#### 2.11.3 Gestión de Clientes
**RF-ADMIN-007:** Visualización de clientes
- Lista de usuarios registrados
- Información: nombre, email, fecha de registro, total de órdenes, total gastado
- Ver historial de órdenes por cliente
- Búsqueda por nombre o email

#### 2.11.4 Gestión de Reviews
**RF-ADMIN-008:** Moderación de reseñas
- Ver todas las reseñas (pendientes, aprobadas, rechazadas)
- Filtros por estado y calificación
- Aprobar reseñas pendientes
- Rechazar reseñas (con motivo)
- Eliminar reseñas
- Ver producto asociado

#### 2.11.5 Reportes y Analíticas
**RF-ADMIN-009:** Dashboard principal
- Métricas clave (KPIs):
  - Ventas del día actual
  - Ventas del mes actual
  - Ventas del año actual
  - Total de órdenes pendientes
  - Total de productos con stock bajo
  - Total de devoluciones pendientes
- Gráficos:
  - Ventas por día (últimos 30 días)
  - Ventas por mes (últimos 12 meses)

**RF-ADMIN-010:** Reporte de ventas
- Filtros por rango de fechas
- Agrupación por:
  - Día
  - Mes
  - Año
- Métricas:
  - Total de ventas (MXN)
  - Total de órdenes
  - Ticket promedio
  - Productos vendidos
- Exportar a CSV/Excel

**RF-ADMIN-011:** Reporte de productos
- Productos más vendidos (cantidad y monto)
- Productos menos vendidos
- Filtros por categoría y rango de fechas
- Exportar a CSV/Excel

**RF-ADMIN-012:** Reporte de inventario
- Stock actual por producto/variante
- Productos con stock bajo (alerta configurable)
- Valor total de inventario
- Productos agotados
- Filtros por categoría
- Exportar a CSV/Excel

**RF-ADMIN-013:** Búsqueda avanzada en reportes
- Búsqueda por nombre de producto
- Búsqueda por categoría
- Filtro por rango de precios
- Combinación de filtros

### 2.12 Módulo de Seguridad

**RF-SEC-001:** Protección contra ataques
- Rate limiting (implementado en Nginx):
  - Login: 5 intentos por minuto por IP
  - Registro: 3 intentos por minuto por IP
  - Checkout: 10 intentos por minuto por usuario
  - API general: 100 requests por minuto por IP
- Request throttling para endpoints críticos

**RF-SEC-002:** Validación de datos
- Sanitización de inputs en todos los endpoints
- Validación de tipos de datos
- Protección contra SQL Injection (uso de PreparedStatements)
- Protección contra XSS (escape de HTML)
- Validación de tamaños de archivo

**RF-SEC-003:** Gestión de sesiones
- Invalidación de sesiones antiguas al cambiar contraseña
- Logout automático tras inactividad (configurable)
- Múltiples sesiones permitidas por usuario
- Registro de actividad de sesiones

**RF-SEC-004:** Logs de auditoría
- Registro de acciones críticas:
  - Login/logout exitoso y fallido
  - Cambios de contraseña
  - Creación/edición/eliminación de productos (admin)
  - Cambios de estado de órdenes
  - Ajustes manuales de inventario
  - Aprobación/rechazo de reviews
- Información registrada: usuario, IP, timestamp, acción, datos modificados

**RF-SEC-005:** Protección de endpoints
- Validación de JWT en todos los endpoints protegidos
- Verificación de roles y permisos
- Endpoints de admin solo accesibles con rol ADMIN

**RF-SEC-006:** Protección de datos sensibles
- Contraseñas hasheadas con BCrypt (factor 12)
- Tokens de sesión seguros (httpOnly, secure, sameSite)
- No exponer información sensible en respuestas de error
- HTTPS obligatorio en producción

### 2.13 Módulo de Notificaciones por Email

**RF-EMAIL-001:** Servicio de email
- Integración con Brevo (Sendinblue) para envío de emails
- Plantillas HTML responsive
- Configuración de remitente verificado

**RF-EMAIL-002:** Tipos de emails
1. Verificación de cuenta
2. Bienvenida tras verificar cuenta
3. Recuperación de contraseña
4. Confirmación de orden (EN_PROCESO)
5. Pago confirmado (PAGADA)
6. Orden enviada (ENVIADA) con número de guía
7. Orden entregada (ENTREGADA)
8. Orden cancelada (CANCELADA)
9. Solicitud de devolución recibida
10. Devolución aprobada/rechazada
11. Bloqueo de cuenta (intentos fallidos)

**RF-EMAIL-003:** Sistema de colas
- Implementar cola asíncrona para envío de emails (Spring @Async)
- Reintentos automáticos en caso de fallo (3 intentos)
- Log de emails enviados/fallidos

---

## 3. REQUERIMIENTOS FUNCIONALES - FRONTEND (REACT)

### 3.1 Arquitectura y Estructura

**RF-FE-001:** Aplicación SPA (Single Page Application)
- React 18+ con React Router v6
- Gestión de estado global con Context API + useReducer (o Redux Toolkit si se requiere)
- Lazy loading de componentes para optimización

**RF-FE-002:** Estructura de rutas
- Rutas públicas:
  - `/` - Home
  - `/productos` - Catálogo general
  - `/productos/:categoria` - Productos por categoría
  - `/producto/:slug` - Detalle de producto
  - `/login` - Inicio de sesión
  - `/registro` - Registro de usuario
  - `/recuperar-password` - Recuperación de contraseña
  - `/verificar-email/:token` - Verificación de email
  - `/carrito` - Carrito de compras
  - `/checkout` - Proceso de pago
  - `/orden/:numero` - Seguimiento de orden (invitados)
  - `/buscar` - Resultados de búsqueda
- Rutas protegidas (requieren autenticación):
  - `/mi-cuenta` - Perfil de usuario
  - `/mis-ordenes` - Historial de órdenes
  - `/mi-cuenta/direcciones` - Gestión de direcciones
  - `/mi-cuenta/wishlist` - Lista de deseos
  - `/orden/:id/detalle` - Detalle de orden
  - `/orden/:id/devolucion` - Solicitar devolución
- Rutas de administrador:
  - `/admin` - Dashboard principal
  - `/admin/productos` - Gestión de productos
  - `/admin/productos/nuevo` - Crear producto
  - `/admin/productos/:id/editar` - Editar producto
  - `/admin/categorias` - Gestión de categorías
  - `/admin/ordenes` - Gestión de órdenes
  - `/admin/ordenes/:id` - Detalle de orden
  - `/admin/devoluciones` - Gestión de devoluciones
  - `/admin/clientes` - Gestión de clientes
  - `/admin/reviews` - Moderación de reseñas
  - `/admin/cupones` - Gestión de cupones
  - `/admin/reportes/ventas` - Reporte de ventas
  - `/admin/reportes/productos` - Reporte de productos
  - `/admin/reportes/inventario` - Reporte de inventario
  - `/admin/configuracion` - Configuraciones generales

### 3.2 Componentes de UI Compartidos

**RF-FE-003:** Sistema de componentes reutilizables
- Botones (primary, secondary, danger, outline)
- Inputs (text, email, password, number, textarea)
- Select/Dropdown
- Checkbox y Radio buttons
- Modal/Dialog
- Alert/Toast notifications
- Loading spinner
- Breadcrumbs
- Pagination
- Tabs
- Accordion
- Cards (producto, orden, etc.)
- Badge/Tag
- Rating stars (interactivo y solo lectura)
- File upload
- Image gallery/carousel
- Data table con ordenamiento y filtros

**RF-FE-004:** Sistema de diseño consistente
- Paleta de colores definida (tema de joyería elegante)
- Tipografía consistente (2-3 familias máximo)
- Espaciado estandarizado (sistema de spacing: 4px base)
- Iconos (librería React Icons o similar)
- Animaciones sutiles (transiciones, hover effects)

### 3.3 Páginas Públicas

#### 3.3.1 Home
**RF-FE-005:** Página principal
- Hero banner (imagen destacada + CTA)
- Productos destacados (slider/grid)
- Categorías principales (grid con imágenes)
- Productos nuevos
- Productos más vendidos
- Sección de beneficios (envío, garantía, etc.)
- Newsletter signup (opcional)
- Footer con enlaces útiles

#### 3.3.2 Catálogo de Productos
**RF-FE-006:** Lista de productos
- Grid responsive de productos (4 columnas desktop, 2 tablet, 1 mobile)
- Card de producto:
  - Imagen principal
  - Nombre del producto
  - Precio (con descuento si aplica)
  - Rating promedio
  - Badge de "nuevo" o "agotado"
  - Botón de agregar a wishlist (si está autenticado)
  - Hover: quick view o botón de agregar al carrito
- Filtros laterales (sidebar en desktop, modal en mobile):
  - Categoría
  - Rango de precio (slider)
  - Material (checkboxes)
  - Color (checkboxes)
  - Marca (checkboxes)
  - Calificación mínima
  - Solo en stock (toggle)
- Barra de ordenamiento (dropdown)
- Resultados encontrados (contador)
- Paginación o scroll infinito
- Breadcrumbs

#### 3.3.3 Detalle de Producto
**RF-FE-007:** Página de producto individual
- Galería de imágenes:
  - Imagen principal (zoom al hacer hover)
  - Thumbnails clickeables
  - Lightbox para ver en grande
  - Videos (si existen)
- Información del producto:
  - Nombre
  - SKU
  - Precio (con descuento si aplica)
  - Rating promedio + número de reviews
  - Descripción corta
  - Selector de variantes (talla, color) si aplica
  - Indicador de stock disponible
  - Cantidad selector (input numérico)
  - Botón "Agregar al carrito"
  - Botón "Agregar a lista de deseos"
  - Información de envío (costo estimado, tiempos)
  - Información de garantía
- Tabs:
  - Descripción detallada
  - Especificaciones (material, peso, etc.)
  - Reviews de clientes
  - Política de devoluciones
- Sección de productos relacionados
- Breadcrumbs

**RF-FE-008:** Sección de reviews
- Resumen de calificaciones (distribución de estrellas)
- Filtro de reviews por calificación
- Lista de reviews:
  - Nombre del usuario
  - Calificación (estrellas)
  - Título del review
  - Comentario
  - Fecha
  - Badge "Compra verificada"
- Paginación
- Formulario de review (solo si usuario compró el producto):
  - Calificación (estrellas interactivas)
  - Título
  - Comentario
  - Botón enviar

#### 3.3.4 Búsqueda
**RF-FE-009:** Barra de búsqueda (header)
- Input con autocompletado
- Sugerencias mientras se escribe (productos populares)
- Icono de búsqueda
- Shortcut de teclado (ej: Ctrl+K)

**RF-FE-010:** Página de resultados de búsqueda
- Similar a catálogo de productos
- Mostrar término de búsqueda
- Mensaje si no hay resultados + sugerencias
- Mismos filtros y ordenamiento

### 3.4 Autenticación

#### 3.4.1 Login
**RF-FE-011:** Página de inicio de sesión
- Formulario:
  - Input email
  - Input password (con toggle show/hide)
  - Checkbox "Recordarme"
  - Botón "Iniciar sesión"
  - Link "¿Olvidaste tu contraseña?"
- Google reCAPTCHA (visible tras 3 intentos fallidos)
- Botones de social login:
  - "Continuar con Google"
  - "Continuar con Facebook"
- Link a página de registro
- Validaciones en tiempo real
- Mensajes de error claros
- Loading state en botón

#### 3.4.2 Registro
**RF-FE-012:** Página de registro
- Formulario:
  - Nombre completo
  - Email
  - Password (con requisitos visibles)
  - Confirmar password
  - Checkbox de términos y condiciones
  - Google reCAPTCHA v3
  - Botón "Crear cuenta"
- Botones de social login
- Link a página de login
- Validaciones en tiempo real:
  - Email válido
  - Password cumple requisitos
  - Passwords coinciden
- Indicador de fortaleza de contraseña
- Mensaje de éxito y redirección a verificar email

#### 3.4.3 Recuperación de Contraseña
**RF-FE-013:** Solicitud de recuperación
- Formulario simple:
  - Input email
  - Botón "Enviar enlace de recuperación"
- Mensaje de éxito (siempre mostrar, no revelar si email existe)
- Link para volver a login

**RF-FE-014:** Restablecimiento de contraseña
- Formulario:
  - Nueva password (con requisitos)
  - Confirmar password
  - Botón "Restablecer contraseña"
- Validación de token en backend
- Mensaje si token expirado/inválido
- Redirección a login tras éxito

#### 3.4.4 Verificación de Email
**RF-FE-015:** Página de verificación
- Validación automática del token al cargar
- Loading state
- Mensaje de éxito o error
- Botón para ir a login o home
- Opción de reenviar email de verificación

### 3.5 Carrito de Compras

**RF-FE-016:** Drawer/Modal de carrito (mini cart)
- Icono en header con badge de cantidad de items
- Al hacer clic, slide-in lateral con:
  - Lista de items en carrito (imagen, nombre, precio, cantidad)
  - Subtotal
  - Botón "Ver carrito"
  - Botón "Checkout"
  - Mensaje si carrito vacío

**RF-FE-017:** Página de carrito completo
- Tabla/lista de items:
  - Imagen del producto
  - Nombre y variantes
  - Precio unitario
  - Selector de cantidad
  - Subtotal por item
  - Botón eliminar
- Resumen de carrito:
  - Subtotal
  - Input para código de cupón (con botón aplicar)
  - Descuento aplicado (si existe)
  - Costo de envío (calculado o "a calcular en checkout")
  - Total
- Botón "Continuar comprando"
- Botón "Proceder al checkout"
- Actualización automática al cambiar cantidades
- Validación de stock en tiempo real
- Persistencia (BD para autenticados, localStorage para invitados)

### 3.6 Checkout

**RF-FE-018:** Proceso de checkout multi-paso
- Paso 1: Información de envío
  - Usuario registrado: seleccionar dirección guardada o agregar nueva
  - Usuario invitado: formulario de dirección + email obligatorio
  - Validación de campos obligatorios
- Paso 2: Método de envío
  - Seleccionar paquetería (DHL o FedEx)
  - Mostrar costo de envío calculado
  - Tiempo estimado de entrega
- Paso 3: Resumen y pago
  - Resumen de productos
  - Dirección de envío
  - Método de envío
  - Cupón aplicado (con opción de quitar)
  - Total desglosado
  - Método de pago:
    - Opción 1: Tarjeta (Stripe Elements integrado)
    - Opción 2: PayPal (botón integrado)
  - Checkbox de términos y condiciones
  - Botón "Realizar pedido"

**RF-FE-019:** Integración de pagos
- Stripe Elements para formulario de tarjeta
- PayPal Smart Payment Buttons
- Loading state durante procesamiento
- Manejo de errores de pago
- Redirección a página de confirmación

**RF-FE-020:** Página de confirmación de orden
- Mensaje de éxito
- Número de orden
- Resumen de la compra
- Información de envío
- Instrucciones de seguimiento
- Botón para ver detalle de orden
- Botón para seguir comprando

### 3.7 Área de Usuario

#### 3.7.1 Perfil
**RF-FE-021:** Mi cuenta - Dashboard
- Menú lateral con opciones:
  - Mi perfil
  - Mis órdenes
  - Direcciones
  - Lista de deseos
  - Cerrar sesión
- Sección de perfil:
  - Foto de perfil (editable)
  - Formulario de datos personales (nombre, email, teléfono)
  - Botón "Guardar cambios"
  - Sección de cambio de contraseña (separada)

#### 3.7.2 Órdenes
**RF-FE-022:** Mis órdenes
- Lista de órdenes:
  - Número de orden
  - Fecha
  - Total
  - Estado (con badge de color)
  - Botón "Ver detalle"
- Filtros por estado
- Búsqueda por número de orden

**RF-FE-023:** Detalle de orden
- Número de orden
- Fecha de compra
- Estado actual (con timeline/stepper visual)
- Lista de productos (imagen, nombre, cantidad, precio)
- Dirección de envío
- Método de pago
- Subtotal, descuento, envío, total
- Información de envío (paquetería, guía, link de tracking) si aplica
- Botón "Cancelar orden" (si estado permite)
- Botón "Solicitar devolución" (si orden entregada)
- Botón "Descargar comprobante"

**RF-FE-024:** Solicitud de devolución
- Formulario:
  - Seleccionar productos a devolver (checkboxes)
  - Motivo de devolución (dropdown)
  - Comentarios adicionales (textarea)
  - Botón "Enviar solicitud"
- Mensaje de confirmación
- Redirección a detalle de orden

#### 3.7.3 Direcciones
**RF-FE-025:** Gestión de direcciones
- Lista de direcciones guardadas:
  - Nombre destinatario
  - Dirección completa
  - Badge "Predeterminada"
  - Botones: Editar, Eliminar, Marcar como predeterminada
- Botón "Agregar nueva dirección"
- Modal de formulario de dirección:
  - Nombre destinatario
  - Calle y número
  - Colonia
  - Código postal
  - Ciudad
  - Estado (dropdown)
  - Teléfono
  - Referencias
  - Checkbox "Marcar como predeterminada"

#### 3.7.4 Lista de Deseos
**RF-FE-026:** Wishlist
- Grid de productos (similar a catálogo)
- Botón "Agregar al carrito" en cada producto
- Botón "Eliminar de wishlist"
- Mensaje si está vacía
- Opción de compartir wishlist (opcional v2)

### 3.8 Panel de Administración

#### 3.8.1 Dashboard Principal
**RF-FE-027:** Dashboard admin - Home
- Tarjetas de KPIs:
  - Ventas del día (MXN)
  - Ventas del mes (MXN)
  - Ventas del año (MXN)
  - Órdenes pendientes (contador)
  - Productos con stock bajo (contador)
  - Devoluciones pendientes (contador)
- Gráfico de ventas (línea/barras):
  - Últimos 30 días (por día)
  - Toggle para ver últimos 12 meses
- Tabla de órdenes recientes
- Tabla de productos más vendidos (top 5)

#### 3.8.2 Gestión de Productos
**RF-FE-028:** Lista de productos (admin)
- Data table con columnas:
  - Imagen
  - Nombre
  - SKU
  - Categoría
  - Precio
  - Stock total
  - Estado (badge)
  - Acciones (Editar, Eliminar)
- Filtros:
  - Categoría
  - Estado (activo, inactivo, agotado)
  - Stock bajo
- Búsqueda por nombre o SKU
- Botón "Crear producto"
- Paginación

**RF-FE-029:** Crear/Editar producto
- Formulario multi-sección:
  - Información básica:
    - Nombre
    - SKU
    - Categoría (dropdown)
    - Marca
    - Precio
    - Precio con descuento (opcional)
    - Estado (activo/inactivo)
  - Descripción:
    - Descripción corta (textarea)
    - Descripción larga (editor de texto enriquecido)
  - Atributos:
    - Material (select múltiple o chips)
    - Garantía (meses)
    - Peso (gramos)
  - Multimedia:
    - Upload de imágenes (drag & drop, máx 10)
    - Previsualización con opción de reordenar
    - Upload de videos (máx 2)
  - Variantes (opcional):
    - Tabla dinámica para agregar variantes
    - Columnas: Talla, Color, SKU variante, Stock, Precio diferencial
    - Botón "Agregar variante"
  - Inventario:
    - Stock inicial (si no hay variantes)
    - Alerta de stock bajo (número)
- Botones: "Guardar", "Guardar y crear otro", "Cancelar"
- Validaciones en tiempo real

**RF-FE-030:** Gestión de categorías
- Lista de categorías (tabla jerárquica)
- CRUD con modal:
  - Nombre
  - Slug (auto-generado, editable)
  - Descripción
  - Categoría padre (dropdown)
  - Imagen
  - Estado (activa/inactiva)
- Drag & drop para reordenar

#### 3.8.3 Gestión de Órdenes
**RF-FE-031:** Lista de órdenes (admin)
- Data table con columnas:
  - Número de orden
  - Fecha
  - Cliente (nombre o email)
  - Total (MXN)
  - Estado (badge)
  - Acciones (Ver detalle)
- Filtros:
  - Estado
  - Rango de fechas
  - Método de pago
- Búsqueda por número de orden o email
- Paginación

**RF-FE-032:** Detalle de orden (admin)
- Similar a vista de usuario, pero con opciones de admin:
  - Dropdown para cambiar estado de orden
  - Formulario para asignar envío:
    - Paquetería (dropdown)
    - Número de guía
    - Botón "Marcar como enviada"
  - Botón "Cancelar orden"
  - Información del cliente (email, teléfono)
  - Notas internas (campo de texto para admin)
  - Timeline de cambios de estado

#### 3.8.4 Gestión de Devoluciones
**RF-FE-033:** Lista de devoluciones
- Data table:
  - Número de devolución
  - Número de orden
  - Cliente
  - Fecha de solicitud
  - Motivo
  - Estado (badge)
  - Acciones (Ver detalle)
- Filtros por estado
- Búsqueda

**RF-FE-034:** Detalle de devolución
- Información de la devolución:
  - Productos solicitados para devolver
  - Motivo y comentarios del cliente
  - Información de la orden original
  - Datos de contacto del cliente
- Notas internas (para admin)
- Acciones:
  - Botón "Aprobar devolución"
  - Botón "Rechazar devolución" (con campo de motivo)
  - Botón "Marcar como completada"
  - Link a panel de reembolso (Stripe/PayPal)

#### 3.8.5 Gestión de Clientes
**RF-FE-035:** Lista de clientes
- Data table:
  - Nombre
  - Email
  - Fecha de registro
  - Total de órdenes
  - Total gastado (MXN)
  - Acciones (Ver detalle)
- Búsqueda por nombre o email
- Paginación

**RF-FE-036:** Detalle de cliente
- Información del cliente
- Historial de órdenes
- Estadísticas (total órdenes, total gastado, ticket promedio)

#### 3.8.6 Moderación de Reviews
**RF-FE-037:** Lista de reviews
- Data table:
  - Producto (imagen + nombre)
  - Cliente
  - Calificación
  - Fecha
  - Estado (badge)
  - Acciones (Aprobar, Rechazar, Eliminar)
- Filtros:
  - Estado (pendiente, aprobado, rechazado)
  - Calificación
- Expandible para ver comentario completo

#### 3.8.7 Gestión de Cupones
**RF-FE-038:** Lista de cupones
- Data table:
  - Código
  - Tipo (porcentaje/monto)
  - Valor
  - Vigencia
  - Usos (actual/máximo)
  - Estado (badge)
  - Acciones (Editar, Eliminar, Activar/Desactivar)
- Botón "Crear cupón"

**RF-FE-039:** Crear/Editar cupón
- Formulario:
  - Código (input, auto mayúsculas)
  - Tipo (radio: porcentaje o monto fijo)
  - Valor del descuento
  - Fecha inicio
  - Fecha fin
  - Usos máximos totales (opcional)
  - Usos máximos por usuario (opcional)
  - Monto mínimo de compra (opcional)
  - Categorías aplicables (multi-select, opcional)
  - Estado (toggle activo/inactivo)
- Validaciones
- Botones: Guardar, Cancelar

#### 3.8.8 Reportes
**RF-FE-040:** Reporte de ventas
- Filtros:
  - Rango de fechas (date picker)
  - Agrupación (día, mes, año)
- Visualización:
  - Gráfico de barras/línea
  - Tabla con detalles
- Métricas:
  - Total de ventas (MXN)
  - Total de órdenes
  - Ticket promedio
  - Total de productos vendidos
- Botón "Exportar a CSV/Excel"

**RF-FE-041:** Reporte de productos
- Filtros:
  - Categoría
  - Rango de fechas
  - Ordenar por (más vendidos, menos vendidos)
- Tabla:
  - Producto
  - Categoría
  - Cantidad vendida
  - Ingresos generados (MXN)
- Botón "Exportar"

**RF-FE-042:** Reporte de inventario
- Filtros:
  - Categoría
  - Estado (todos, stock bajo, agotados)
- Tabla:
  - Producto
  - SKU
  - Categoría
  - Stock actual
  - Valor unitario
  - Valor total
- Métricas:
  - Valor total de inventario
  - Total de productos
  - Productos con stock bajo
  - Productos agotados
- Botón "Exportar"

### 3.9 Diseño Responsive

**RF-FE-043:** Breakpoints
- Mobile: < 640px
- Tablet: 640px - 1024px
- Desktop: > 1024px

**RF-FE-044:** Adaptaciones móviles
- Navegación hamburger en mobile
- Filtros en modal/drawer en mobile
- Tablas en cards apiladas en mobile
- Carrito slide-in lateral en todas las pantallas
- Forms en una sola columna en mobile
- Admin dashboard: menú colapsable lateral

### 3.10 Performance y Optimización

**RF-FE-045:** Optimizaciones
- Lazy loading de imágenes
- Code splitting por rutas
- Minificación de assets
- Compresión de imágenes (WebP con fallback)
- Caché de assets estáticos
- Debounce en búsquedas
- Throttle en scroll infinito

**RF-FE-046:** SEO básico
- Meta tags dinámicos por página
- Open Graph tags para productos
- Sitemap generado
- Breadcrumbs con structured data
- URLs amigables (slugs)

### 3.11 Accesibilidad

**RF-FE-047:** Estándares de accesibilidad
- Navegación por teclado
- Contraste de colores (WCAG AA)
- Etiquetas ARIA donde sea necesario
- Alt text en imágenes
- Focus visible
- Formularios con labels asociados

### 3.12 Gestión de Estado y Datos

**RF-FE-048:** Manejo de estado global
- Autenticación (usuario actual, tokens)
- Carrito de compras
- Wishlist
- Configuraciones de UI (tema, idioma si aplica)

**RF-FE-049:** Comunicación con backend
- Axios o Fetch API
- Interceptores para:
  - Agregar JWT a headers
  - Refresh token automático
  - Manejo global de errores
- Loading states
- Error handling y retry logic

---

## 4. REQUERIMIENTOS NO FUNCIONALES

### 4.1 Seguridad

**RNF-SEC-001:** Autenticación y autorización
- JWT con RS256 (claves asimétricas)
- Refresh token rotation
- Tokens almacenados en httpOnly cookies (frontend)
- Validación de tokens en cada request protegido

**RNF-SEC-002:** Comunicación segura
- HTTPS obligatorio en producción
- Secure cookies
- CORS configurado correctamente
- HSTS headers

**RNF-SEC-003:** Protección de datos
- Passwords hasheadas con BCrypt (factor 12)
- No logs de información sensible
- Datos de pago manejados por Stripe/PayPal (PCI DSS compliant)
- Sanitización de inputs

**RNF-SEC-004:** Rate limiting (Nginx)
- Login: 5 requests/minuto/IP
- Registro: 3 requests/minuto/IP
- Checkout: 10 requests/minuto/usuario
- API general: 100 requests/minuto/IP

**RNF-SEC-005:** Protección contra ataques
- SQL Injection: PreparedStatements
- XSS: sanitización y escape de HTML
- CSRF: tokens CSRF en forms críticos
- Clickjacking: X-Frame-Options header

### 4.2 Performance

**RNF-PERF-001:** Tiempos de respuesta
- APIs: < 500ms (p95)
- Carga de página: < 3s (3G)
- Interactividad (TTI): < 5s

**RNF-PERF-002:** Escalabilidad
- Soportar 500 usuarios concurrentes
- Capacidad de crecer a 2000 usuarios sin cambios arquitectónicos
- Consultas a BD optimizadas (índices, queries eficientes)
- Caché de datos estáticos (categorías, configuraciones)

**RNF-PERF-003:** Optimización de base de datos
- Índices en columnas de búsqueda frecuente
- Paginación en queries grandes
- Connection pooling
- Consultas optimizadas (evitar N+1)

### 4.3 Disponibilidad y Confiabilidad

**RNF-AVAIL-001:** Disponibilidad
- Uptime objetivo: 99% (permite ~7h downtime/mes)
- Monitoreo de salud del servicio
- Logs centralizados

**RNF-AVAIL-002:** Backup
- Backup diario de base de datos
- Retención de backups: 30 días
- Backup de archivos multimedia semanal

**RNF-AVAIL-003:** Manejo de errores
- Mensajes de error claros para el usuario
- Logs detallados para debugging
- Páginas de error personalizadas (404, 500)

### 4.4 Usabilidad

**RNF-USA-001:** Experiencia de usuario
- Interfaz intuitiva y consistente
- Feedback visual en todas las acciones
- Tiempos de carga < 3s
- Mensajes de error descriptivos
- Confirmaciones en acciones destructivas

**RNF-USA-002:** Responsive design
- Funcional en todos los dispositivos
- Touch-friendly en móviles
- Textos legibles sin zoom

### 4.5 Mantenibilidad

**RNF-MAINT-001:** Código limpio
- Nomenclatura clara y consistente
- Comentarios en lógica compleja
- Modularización de código
- Separación de responsabilidades

**RNF-MAINT-002:** Documentación
- README con instrucciones de setup
- Documentación de APIs (Swagger/OpenAPI)
- Diagramas de arquitectura
- Guía de deployment

**RNF-MAINT-003:** Testing
- Unit tests para lógica crítica (backend)
- Integration tests para APIs principales
- E2E tests para flujos críticos (checkout, login)
- Cobertura mínima: 70%

### 4.6 Compatibilidad

**RNF-COMPAT-001:** Navegadores soportados
- Chrome (últimas 2 versiones)
- Firefox (últimas 2 versiones)
- Safari (últimas 2 versiones)
- Edge (últimas 2 versiones)

**RNF-COMPAT-002:** Dispositivos
- iOS 13+
- Android 8+
- Desktop (Windows, macOS, Linux)

### 4.7 Internacionalización (futuro)

**RNF-I18N-001:** Preparación para multi-idioma
- Textos externalizados en archivos de traducción
- Formato de moneda configurable
- Formato de fechas configurable

---

## 5. ARQUITECTURA TÉCNICA

### 5.1 Stack Tecnológico

#### Backend
- **Lenguaje:** Java 17+
- **Framework:** Spring Boot 3.x
  - Spring Web (REST APIs)
  - Spring Data JPA (ORM)
  - Spring Security (autenticación/autorización)
  - Spring Validation (validaciones)
  - Spring Mail (envío de emails)
- **Base de datos:** PostgreSQL 15+
- **ORM:** Hibernate
- **Migración de BD:** Flyway o Liquibase
- **Build tool:** Maven o Gradle
- **Documentación API:** Springdoc OpenAPI (Swagger)

#### Frontend
- **Framework:** React 18+
- **Lenguaje:** JavaScript (ES6+) o TypeScript (recomendado)
- **Router:** React Router v6
- **HTTP Client:** Axios
- **Gestión de estado:** Context API + useReducer (o Redux Toolkit)
- **UI Components:** 
  - Headless UI o Radix UI (componentes accesibles)
  - Tailwind CSS (estilos)
- **Forms:** React Hook Form + Yup/Zod (validaciones)
- **Build tool:** Vite
- **Pagos:**
  - @stripe/react-stripe-js
  - @paypal/react-paypal-js

#### Infraestructura
- **Reverse Proxy / API Gateway:** Nginx
  - Rate limiting
  - Request throttling
  - SSL termination
  - Static file serving
- **Almacenamiento de archivos:** Sistema de archivos local (inicialmente)
- **Email:** Brevo (Sendinblue) SMTP
- **Hosting:** Cloud VPS (DigitalOcean, Linode, AWS EC2, etc.)
- **CI/CD:** GitHub Actions o GitLab CI (opcional)

### 5.2 Arquitectura del Sistema

#### Patrón de arquitectura: Monolito modular en capas

**Capas del backend:**
1. **Presentation Layer** (Controllers)
   - REST Controllers
   - Request/Response DTOs
   - Validaciones de entrada
   - Manejo de errores HTTP

2. **Service Layer** (Business Logic)
   - Servicios de negocio
   - Orquestación de operaciones
   - Validaciones de negocio
   - Transacciones

3. **Data Access Layer** (Repositories)
   - Spring Data JPA Repositories
   - Queries personalizadas
   - Especificaciones para filtrado dinámico

4. **Domain Layer** (Entities)
   - Entidades JPA
   - Modelos de dominio
   - Value Objects

**Módulos del backend:**
- `auth` - Autenticación y autorización
- `user` - Gestión de usuarios
- `product` - Gestión de productos, categorías, variantes
- `cart` - Carrito de compras
- `order` - Órdenes, checkout, pagos
- `inventory` - Control de inventario
- `shipping` - Gestión de envíos
- `coupon` - Cupones y promociones
- `review` - Reseñas y calificaciones
- `notification` - Emails y notificaciones
- `admin` - Funcionalidades de administración
- `report` - Generación de reportes

**Estructura del frontend:**
```
src/
├── components/
│   ├── common/         # Componentes reutilizables
│   ├── layout/         # Header, Footer, Sidebar
│   ├── product/        # Componentes de producto
│   ├── cart/           # Componentes de carrito
│   └── admin/          # Componentes del admin
├── pages/              # Páginas/Rutas
├── hooks/              # Custom hooks
├── context/            # Context providers
├── services/           # API calls
├── utils/              # Utilidades
├── assets/             # Imágenes, iconos, etc.
└── styles/             # Estilos globales
```

### 5.3 Base de Datos

**Entidades principales:**
- `users` - Usuarios del sistema
- `roles` - Roles (GUEST, USER, ADMIN)
- `addresses` - Direcciones de envío
- `categories` - Categorías de productos
- `products` - Productos
- `product_variants` - Variantes de productos
- `product_images` - Imágenes de productos
- `product_videos` - Videos de productos
- `materials` - Materiales (tabla catálogo)
- `product_materials` - Relación many-to-many
- `carts` - Carritos de usuarios autenticados
- `cart_items` - Items del carrito
- `orders` - Órdenes de compra
- `order_items` - Items de la orden
- `order_status_history` - Historial de cambios de estado
- `payments` - Información de pagos
- `shipments` - Información de envíos
- `coupons` - Cupones de descuento
- `coupon_usage` - Uso de cupones
- `reviews` - Reseñas de productos
- `wishlists` - Lista de deseos
- `wishlist_items` - Items de wishlist
- `returns` - Devoluciones
- `inventory_movements` - Movimientos de inventario
- `audit_logs` - Logs de auditoría

**Estrategia de índices:**
- Índices en claves foráneas
- Índices en campos de búsqueda (email, sku, slug)
- Índices compuestos para filtros frecuentes
- Índice full-text en nombre y descripción de productos

### 5.4 APIs

**Estilo:** RESTful

**Convención de URLs:**
- `GET /api/products` - Listar productos
- `GET /api/products/{id}` - Obtener producto
- `POST /api/products` - Crear producto (admin)
- `PUT /api/products/{id}` - Actualizar producto (admin)
- `DELETE /api/products/{id}` - Eliminar producto (admin)

**Autenticación:**
- Header: `Authorization: Bearer {access_token}`

**Formato de respuestas:**
```json
{
  "success": true,
  "data": { ... },
  "message": "Operación exitosa"
}
```

**Formato de errores:**
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Datos inválidos",
    "details": [
      {
        "field": "email",
        "message": "Email inválido"
      }
    ]
  }
}
```

**Versionado:**
- V1: `/api/v1/...` (opcional, para facilitar futuras versiones)

### 5.5 Seguridad de APIs

- CORS configurado para solo frontend permitido
- Rate limiting en Nginx
- Validación de inputs en todos los endpoints
- SQL Injection prevention (PreparedStatements)
- XSS prevention (sanitización)
- CSRF tokens en operaciones sensibles
- Logs de actividad sospechosa

---

## 6. FLUJOS DE USUARIO PRINCIPALES

### 6.1 Compra como Usuario Registrado
1. Usuario navega catálogo o busca productos
2. Filtra/ordena resultados
3. Ve detalle de producto
4. Selecciona variantes (si aplica)
5. Agrega al carrito
6. Ve carrito, ajusta cantidades
7. Aplica cupón (opcional)
8. Click "Proceder al checkout"
9. Sistema verifica autenticación
10. Selecciona dirección de envío (o agrega nueva)
11. Selecciona método de envío
12. Ve resumen y total
13. Selecciona método de pago
14. Completa pago (Stripe/PayPal)
15. Sistema crea orden, procesa pago
16. Recibe confirmación y email
17. Puede rastrear orden desde "Mis órdenes"

### 6.2 Compra como Invitado
1-6. (Igual que usuario registrado)
7. Click "Proceder al checkout"
8. Sistema detecta no autenticado
9. Opción de registrarse/login o continuar como invitado
10. Ingresa email y confirma
11. Ingresa dirección de envío completa
12-16. (Igual que usuario registrado)
17. Recibe link único en email para rastrear orden

### 6.3 Gestión de Orden (Admin)
1. Admin ingresa a dashboard
2. Ve listado de órdenes
3. Filtra por estado "Pagada"
4. Selecciona una orden
5. Ve detalles completos
6. Prepara productos
7. Selecciona paquetería (DHL/FedEx)
8. Ingresa número de guía
9. Cambia estado a "Enviada"
10. Sistema envía email al cliente con tracking
11. Una vez confirmada entrega, cambia a "Entregada"
12. Cliente puede dejar review

### 6.4 Solicitud de Devolución
1. Cliente ingresa a "Mis órdenes"
2. Selecciona orden entregada
3. Click "Solicitar devolución"
4. Selecciona productos a devolver
5. Indica motivo
6. Agrega comentarios
7. Envía solicitud
8. Admin ve solicitud en dashboard de devoluciones
9. Admin revisa y contacta por email
10. Admin aprueba/rechaza
11. Si aprueba, cliente devuelve producto
12. Admin confirma recepción
13. Admin procesa reembolso
14. Marca devolución como completada

---

## 7. CONSIDERACIONES DE DEPLOYMENT

### 7.1 Entorno de Producción

**Servidor:**
- VPS con al menos 4GB RAM, 2 vCPUs
- Sistema operativo: Ubuntu 22.04 LTS
- Java 17 JRE instalado
- PostgreSQL 15+ instalado
- Nginx instalado

**Estructura:**
```
/var/www/
├── backend/
│   └── ecommerce.jar
├── frontend/
│   └── build/
└── uploads/
    ├── products/
    └── temp/
```

**Nginx como reverse proxy:**
- Sirve frontend (archivos estáticos)
- Proxy a backend en puerto 8080
- SSL/TLS con Let's Encrypt
- Rate limiting configurado
- Compresión gzip
- Caché de estáticos

### 7.2 Variables de Entorno

**Backend (application.properties o .env):**
```
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=ecommerce
DB_USER=ecommerce_user
DB_PASSWORD=***

# JWT
JWT_SECRET=***
JWT_ACCESS_EXPIRATION=900000  # 15 min
JWT_REFRESH_EXPIRATION=604800000  # 7 días

# OAuth
GOOGLE_CLIENT_ID=***
GOOGLE_CLIENT_SECRET=***
FACEBOOK_CLIENT_ID=***
FACEBOOK_CLIENT_SECRET=***

# Stripe
STRIPE_PUBLIC_KEY=***
STRIPE_SECRET_KEY=***
STRIPE_WEBHOOK_SECRET=***

# PayPal
PAYPAL_CLIENT_ID=***
PAYPAL_CLIENT_SECRET=***
PAYPAL_MODE=live

# Email
EMAIL_HOST=smtp-relay.sendinblue.com
EMAIL_PORT=587
EMAIL_USERNAME=***
EMAIL_PASSWORD=***
EMAIL_FROM=noreply@tujoyeria.com

# File uploads
UPLOAD_DIR=/var/www/uploads
MAX_FILE_SIZE=5242880  # 5MB

# App
APP_URL=https://tujoyeria.com
FRONTEND_URL=https://tujoyeria.com
```

**Frontend (.env):**
```
REACT_APP_API_URL=https://tujoyeria.com/api
REACT_APP_STRIPE_PUBLIC_KEY=***
REACT_APP_PAYPAL_CLIENT_ID=***
REACT_APP_GOOGLE_CLIENT_ID=***
REACT_APP_FACEBOOK_APP_ID=***
REACT_APP_RECAPTCHA_SITE_KEY=***
```

### 7.3 Proceso de Deployment

1. **Backend:**
   - Compilar con Maven/Gradle: `./mvnw clean package`
   - Generar JAR: `target/ecommerce-1.0.0.jar`
   - Subir a servidor
   - Ejecutar con systemd service
   - Verificar health endpoint

2. **Frontend:**
   - Build de producción: `npm run build`
   - Generar carpeta `build/`
   - Subir a servidor
   - Nginx sirve archivos estáticos

3. **Base de datos:**
   - Ejecutar migraciones con Flyway/Liquibase
   - Verificar integridad

### 7.4 Monitoring y Logs

- Logs de aplicación: `/var/log/ecommerce/`
- Logs de Nginx: `/var/log/nginx/`
- Logs de PostgreSQL: `/var/log/postgresql/`
- Health check endpoint: `/api/health`
- Monitoreo básico con scripts o herramientas como Uptime Robot

---

## 8. FASES DE DESARROLLO SUGERIDAS

### Fase 1: MVP (4-6 semanas)
- Autenticación básica (email/password)
- CRUD de productos (sin variantes)
- Catálogo y detalle de producto
- Carrito básico
- Checkout con Stripe
- Órdenes básicas
- Panel de admin básico (productos, órdenes)
- Responsive básico

### Fase 2: Funcionalidades Core (3-4 semanas)
- OAuth (Google, Facebook)
- Variantes de productos
- Sistema de reviews
- Lista de deseos
- Gestión de direcciones
- Cupones de descuento
- PayPal
- Notificaciones por email
- Admin dashboard completo

### Fase 3: Optimización y Features Avanzadas (2-3 semanas)
- Sistema de recomendaciones
- Reportes y analíticas
- Devoluciones
- Búsqueda y filtros avanzados
- Optimización de performance
- Testing completo
- SEO básico

### Fase 4: Pulido y Lanzamiento (1-2 semanas)
- Refinamiento de UI/UX
- Corrección de bugs
- Testing end-to-end
- Documentación
- Deployment a producción
- Monitoreo y ajustes

---

## 9. ESTIMACIÓN DE COSTOS INICIALES

### Hosting (mensual)
- VPS 4GB RAM / 2 vCPU: $15-25 USD
- Dominio: $10-15 USD/año
- SSL (Let's Encrypt): Gratis
- **Total hosting: ~$20 USD/mes**

### Servicios externos (mensual, estimado para tráfico bajo)
- Brevo (300 emails/día gratis): $0-25 USD
- Stripe (2.9% + $0.30 por transacción): Variable
- PayPal (comisiones similares): Variable
- **Total servicios: $0-50 USD/mes** (+ comisiones por venta)

### Desarrollo
- Si lo haces tú: $0 (tu tiempo)
- Freelancer: $3000-8000 USD
- Agencia: $10000-20000 USD

---

## 10. MÉTRICAS DE ÉXITO

### KPIs a monitorear post-lanzamiento:
- Tasa de conversión (visitas → compras)
- Ticket promedio
- Tasa de abandono de carrito
- Tiempo promedio en sitio
- Productos más vistos/vendidos
- Tasa de devoluciones
- Satisfacción del cliente (reviews promedio)
- Uptime del sistema
- Tiempo de respuesta de APIs

---

## 11. CONSIDERACIONES FINALES

### Escalabilidad futura:
- Si crece el tráfico: migrar a arquitectura de microservicios
- Si crece el catálogo: implementar Elasticsearch para búsqueda
- Si crece almacenamiento: migrar a S3/Cloudinary
- Si necesita más emails: migrar a AWS SES

### Funcionalidades futuras (post-MVP):
- Programa de puntos/lealtad
- Wishlist compartida
- Comparador de productos
- Chat de soporte
- Blog integrado
- Multi-moneda
- Multi-idioma
- App móvil nativa
- Notificaciones push
- Integración con redes sociales (Instagram shopping)

---

**Fin del documento de requerimientos v1.0**
