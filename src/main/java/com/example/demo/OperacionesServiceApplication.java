package com.example.demo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class OperacionesServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OperacionesServiceApplication.class, args);
	}

}

record Tour(Long id, String nombre, String descripcion, Integer cupoMaximo, BigDecimal precio, Boolean activo) {
}

record SalidaTour(
		Long id,
		Long tourId,
		String fecha,
		String horaInicio,
		String horaFin,
		Integer cupoTotal,
		Integer cupoDisponible,
		String estado,
		Long guiaId,
		Long vehiculoId,
		List<Long> bicicletaIds) {
}

record Guia(Long id, String nombre, String telefono, Boolean activo) {
}

record Vehiculo(Long id, String patente, String modelo, Integer capacidad, Boolean activo) {
}

record Bicicleta(Long id, String codigo, String estado) {
}

record ReservaTour(Long id, Long salidaId, Long reservaHospedajeId, String clienteNombre, Integer pasajeros, String estado) {
}

record AsignarGuiaRequest(Long guiaId) {
}

record AsignarVehiculoRequest(Long vehiculoId) {
}

record AsignarBicicletaRequest(Long bicicletaId) {
}

@Service
class OperacionesStore {

	private final AtomicLong tourIds = new AtomicLong();
	private final AtomicLong salidaIds = new AtomicLong();
	private final AtomicLong guiaIds = new AtomicLong();
	private final AtomicLong vehiculoIds = new AtomicLong();
	private final AtomicLong bicicletaIds = new AtomicLong();
	private final AtomicLong reservaTourIds = new AtomicLong();

	private final Map<Long, Tour> tours = new ConcurrentHashMap<>();
	private final Map<Long, SalidaTour> salidas = new ConcurrentHashMap<>();
	private final Map<Long, Guia> guias = new ConcurrentHashMap<>();
	private final Map<Long, Vehiculo> vehiculos = new ConcurrentHashMap<>();
	private final Map<Long, Bicicleta> bicicletas = new ConcurrentHashMap<>();
	private final Map<Long, ReservaTour> reservasTour = new ConcurrentHashMap<>();

	@PostConstruct
	void cargarDatosIniciales() {
		// --- Tours disponibles (activo=true) ---
		Tour astroturismo = createTour(new Tour(null, "Astroturismo Atacama", "Observacion del cielo atacameno con telescopios profesionales", 12,
				new BigDecimal("45000"), true));
		Tour valleLuna = createTour(new Tour(null, "Valle de la Luna", "Tour de tarde por miradores y formaciones de sal", 10,
				new BigDecimal("35000"), true));
		Tour trekking = createTour(new Tour(null, "Trekking Altiplano", "Caminata por lagunas altoandinas y fauna silvestre", 8,
				new BigDecimal("55000"), true));
		Tour sandboard = createTour(new Tour(null, "Sandboard Medanos", "Descenso en tabla por las dunas de arena", 15,
				new BigDecimal("28000"), true));

		// --- Tours no disponibles (activo=false, temporada cerrada o suspendidos) ---
		Tour tatio = createTour(new Tour(null, "Geysers del Tatio", "Visita al campo geotermal al amanecer", 20,
				new BigDecimal("65000"), false));
		Tour laguna = createTour(new Tour(null, "Laguna Cejar y Tebinquinche", "Flotacion en lagunas de sal y atardecer", 12,
				new BigDecimal("42000"), false));

		// --- Guias ---
		Guia guia1 = createGuia(new Guia(null, "Camila Rojas", "+56911111111", true));
		Guia guia2 = createGuia(new Guia(null, "Andres Cifuentes", "+56922222222", true));
		Guia guia3 = createGuia(new Guia(null, "Lorena Pizarro", "+56933333333", true));

		// --- Vehiculos ---
		Vehiculo van1 = createVehiculo(new Vehiculo(null, "ATC-001", "Van turismo", 12, true));
		Vehiculo van2 = createVehiculo(new Vehiculo(null, "ATC-002", "Minibus", 20, true));
		Vehiculo jeep = createVehiculo(new Vehiculo(null, "ATC-003", "Jeep 4x4", 6, true));

		// --- Bicicletas ---
		Bicicleta bici1 = createBicicleta(new Bicicleta(null, "BICI-001", "DISPONIBLE"));
		Bicicleta bici2 = createBicicleta(new Bicicleta(null, "BICI-002", "DISPONIBLE"));
		Bicicleta bici3 = createBicicleta(new Bicicleta(null, "BICI-003", "MANTENIMIENTO"));

		// --- Salidas programadas (PROGRAMADA) ---
		createSalida(new SalidaTour(null, astroturismo.id(), LocalDate.now().plusDays(1).toString(),
				"21:00", "23:30", 12, null, "PROGRAMADA", guia1.id(), van1.id(), List.of(bici1.id())));
		createSalida(new SalidaTour(null, valleLuna.id(), LocalDate.now().plusDays(2).toString(),
				"16:00", "19:30", 10, null, "PROGRAMADA", guia2.id(), jeep.id(), List.of()));
		createSalida(new SalidaTour(null, trekking.id(), LocalDate.now().plusDays(3).toString(),
				"07:00", "13:00", 8, null, "PROGRAMADA", guia3.id(), jeep.id(), List.of(bici1.id(), bici2.id())));
		createSalida(new SalidaTour(null, sandboard.id(), LocalDate.now().plusDays(4).toString(),
				"10:00", "14:00", 15, null, "PROGRAMADA", guia1.id(), van2.id(), List.of()));
		createSalida(new SalidaTour(null, astroturismo.id(), LocalDate.now().plusDays(5).toString(),
				"21:00", "23:30", 12, null, "PROGRAMADA", guia2.id(), van1.id(), List.of()));
		createSalida(new SalidaTour(null, valleLuna.id(), LocalDate.now().plusDays(7).toString(),
				"16:00", "19:30", 10, null, "PROGRAMADA", null, null, List.of()));

		// --- Salidas canceladas (CANCELADA) ---
		SalidaTour salidaCancelada1 = createSalida(new SalidaTour(null, trekking.id(),
				LocalDate.now().minusDays(3).toString(), "07:00", "13:00", 8, null, "PROGRAMADA", guia3.id(), jeep.id(), List.of()));
		cancelarSalida(salidaCancelada1.id());

		SalidaTour salidaCancelada2 = createSalida(new SalidaTour(null, sandboard.id(),
				LocalDate.now().minusDays(1).toString(), "10:00", "14:00", 15, null, "PROGRAMADA", guia1.id(), van2.id(), List.of()));
		cancelarSalida(salidaCancelada2.id());
	}

	List<Tour> findTours() {
		return tours.values().stream().sorted(Comparator.comparing(Tour::id)).toList();
	}

	Optional<Tour> findTour(Long id) {
		return Optional.ofNullable(tours.get(id));
	}

	Tour createTour(Tour input) {
		Tour saved = new Tour(tourIds.incrementAndGet(), textOr(input.nombre(), "Tour sin nombre"),
				input.descripcion(), numberOr(input.cupoMaximo(), 0), moneyOr(input.precio()),
				boolOr(input.activo(), true));
		tours.put(saved.id(), saved);
		return saved;
	}

	Optional<Tour> updateTour(Long id, Tour input) {
		if (!tours.containsKey(id)) {
			return Optional.empty();
		}

		Tour saved = new Tour(id, textOr(input.nombre(), "Tour sin nombre"), input.descripcion(),
				numberOr(input.cupoMaximo(), 0), moneyOr(input.precio()), boolOr(input.activo(), true));
		tours.put(id, saved);
		return Optional.of(saved);
	}

	List<SalidaTour> findSalidas(String fecha) {
		return salidas.values().stream()
				.filter(salida -> fecha == null || fecha.equals(salida.fecha()))
				.sorted(Comparator.comparing(SalidaTour::id))
				.toList();
	}

	List<SalidaTour> findAgendaGuia(Long guiaId) {
		return salidas.values().stream()
				.filter(salida -> guiaId.equals(salida.guiaId()))
				.sorted(Comparator.comparing(SalidaTour::fecha).thenComparing(SalidaTour::horaInicio))
				.toList();
	}

	Optional<SalidaTour> findSalida(Long id) {
		return Optional.ofNullable(salidas.get(id));
	}

	SalidaTour createSalida(SalidaTour input) {
		int cupoTotal = numberOr(input.cupoTotal(), 0);
		int cupoDisponible = input.cupoDisponible() == null ? cupoTotal : input.cupoDisponible();
		SalidaTour saved = new SalidaTour(salidaIds.incrementAndGet(), input.tourId(), textOr(input.fecha(),
				LocalDate.now().toString()), input.horaInicio(), input.horaFin(), cupoTotal, cupoDisponible,
				textOr(input.estado(), "PROGRAMADA"), input.guiaId(), input.vehiculoId(), safeIds(input.bicicletaIds()));
		salidas.put(saved.id(), saved);
		return saved;
	}

	Optional<SalidaTour> cancelarSalida(Long id) {
		SalidaTour current = salidas.get(id);
		if (current == null) {
			return Optional.empty();
		}

		SalidaTour saved = new SalidaTour(current.id(), current.tourId(), current.fecha(), current.horaInicio(),
				current.horaFin(), current.cupoTotal(), current.cupoDisponible(), "CANCELADA", current.guiaId(),
				current.vehiculoId(), current.bicicletaIds());
		salidas.put(id, saved);
		return Optional.of(saved);
	}

	Optional<SalidaTour> assignGuia(Long salidaId, Long guiaId) {
		if (salidaId == null || guiaId == null || !salidas.containsKey(salidaId) || !guias.containsKey(guiaId)) {
			return Optional.empty();
		}

		SalidaTour current = salidas.get(salidaId);
		SalidaTour saved = new SalidaTour(current.id(), current.tourId(), current.fecha(), current.horaInicio(),
				current.horaFin(), current.cupoTotal(), current.cupoDisponible(), current.estado(), guiaId,
				current.vehiculoId(), current.bicicletaIds());
		salidas.put(salidaId, saved);
		return Optional.of(saved);
	}

	Optional<SalidaTour> assignVehiculo(Long salidaId, Long vehiculoId) {
		if (salidaId == null || vehiculoId == null || !salidas.containsKey(salidaId) || !vehiculos.containsKey(vehiculoId)) {
			return Optional.empty();
		}

		SalidaTour current = salidas.get(salidaId);
		SalidaTour saved = new SalidaTour(current.id(), current.tourId(), current.fecha(), current.horaInicio(),
				current.horaFin(), current.cupoTotal(), current.cupoDisponible(), current.estado(), current.guiaId(),
				vehiculoId, current.bicicletaIds());
		salidas.put(salidaId, saved);
		return Optional.of(saved);
	}

	Optional<SalidaTour> assignBicicleta(Long salidaId, Long bicicletaId) {
		if (salidaId == null || bicicletaId == null || !salidas.containsKey(salidaId) || !bicicletas.containsKey(bicicletaId)) {
			return Optional.empty();
		}

		SalidaTour current = salidas.get(salidaId);
		List<Long> ids = new ArrayList<>(safeIds(current.bicicletaIds()));
		if (!ids.contains(bicicletaId)) {
			ids.add(bicicletaId);
		}

		SalidaTour saved = new SalidaTour(current.id(), current.tourId(), current.fecha(), current.horaInicio(),
				current.horaFin(), current.cupoTotal(), current.cupoDisponible(), current.estado(), current.guiaId(),
				current.vehiculoId(), ids);
		salidas.put(salidaId, saved);
		return Optional.of(saved);
	}

	List<Guia> findGuias() {
		return guias.values().stream().sorted(Comparator.comparing(Guia::id)).toList();
	}

	Optional<Guia> findGuia(Long id) {
		return Optional.ofNullable(guias.get(id));
	}

	Guia createGuia(Guia input) {
		Guia saved = new Guia(guiaIds.incrementAndGet(), textOr(input.nombre(), "Guia sin nombre"), input.telefono(),
				boolOr(input.activo(), true));
		guias.put(saved.id(), saved);
		return saved;
	}

	List<Vehiculo> findVehiculos() {
		return vehiculos.values().stream().sorted(Comparator.comparing(Vehiculo::id)).toList();
	}

	Vehiculo createVehiculo(Vehiculo input) {
		Vehiculo saved = new Vehiculo(vehiculoIds.incrementAndGet(), textOr(input.patente(), "SIN-PATENTE"),
				input.modelo(), numberOr(input.capacidad(), 0), boolOr(input.activo(), true));
		vehiculos.put(saved.id(), saved);
		return saved;
	}

	Optional<Vehiculo> updateVehiculo(Long id, Vehiculo input) {
		if (!vehiculos.containsKey(id)) {
			return Optional.empty();
		}

		Vehiculo saved = new Vehiculo(id, textOr(input.patente(), "SIN-PATENTE"), input.modelo(),
				numberOr(input.capacidad(), 0), boolOr(input.activo(), true));
		vehiculos.put(id, saved);
		return Optional.of(saved);
	}

	List<Bicicleta> findBicicletas() {
		return bicicletas.values().stream().sorted(Comparator.comparing(Bicicleta::id)).toList();
	}

	Bicicleta createBicicleta(Bicicleta input) {
		Bicicleta saved = new Bicicleta(bicicletaIds.incrementAndGet(), textOr(input.codigo(), "SIN-CODIGO"),
				textOr(input.estado(), "DISPONIBLE"));
		bicicletas.put(saved.id(), saved);
		return saved;
	}

	Optional<Bicicleta> updateBicicleta(Long id, Bicicleta input) {
		if (!bicicletas.containsKey(id)) {
			return Optional.empty();
		}

		Bicicleta saved = new Bicicleta(id, textOr(input.codigo(), "SIN-CODIGO"), textOr(input.estado(), "DISPONIBLE"));
		bicicletas.put(id, saved);
		return Optional.of(saved);
	}

	List<ReservaTour> findReservasTour(Long reservaHospedajeId) {
		return reservasTour.values().stream()
				.filter(reserva -> reservaHospedajeId == null || reservaHospedajeId.equals(reserva.reservaHospedajeId()))
				.sorted(Comparator.comparing(ReservaTour::id))
				.toList();
	}

	Optional<ReservaTour> findReservaTour(Long id) {
		return Optional.ofNullable(reservasTour.get(id));
	}

	Optional<ReservaTour> createReservaTour(ReservaTour input) {
		if (input.salidaId() == null) {
			return Optional.empty();
		}

		SalidaTour salida = salidas.get(input.salidaId());
		if (salida == null || !"PROGRAMADA".equals(salida.estado())) {
			return Optional.empty();
		}

		int pasajeros = numberOr(input.pasajeros(), 1);
		if (salida.cupoDisponible() < pasajeros) {
			return Optional.empty();
		}

		SalidaTour updatedSalida = new SalidaTour(salida.id(), salida.tourId(), salida.fecha(), salida.horaInicio(),
				salida.horaFin(), salida.cupoTotal(), salida.cupoDisponible() - pasajeros, salida.estado(), salida.guiaId(),
				salida.vehiculoId(), salida.bicicletaIds());
		salidas.put(salida.id(), updatedSalida);

		ReservaTour saved = new ReservaTour(reservaTourIds.incrementAndGet(), input.salidaId(),
				input.reservaHospedajeId(), textOr(input.clienteNombre(), "Cliente"), pasajeros, "ACTIVA");
		reservasTour.put(saved.id(), saved);
		return Optional.of(saved);
	}

	Optional<ReservaTour> cancelarReservaTour(Long id) {
		ReservaTour reserva = reservasTour.get(id);
		if (reserva == null) {
			return Optional.empty();
		}

		if (!"CANCELADA".equals(reserva.estado())) {
			SalidaTour salida = salidas.get(reserva.salidaId());
			if (salida != null) {
				int cupoDisponible = Math.min(salida.cupoTotal(), salida.cupoDisponible() + reserva.pasajeros());
				salidas.put(salida.id(), new SalidaTour(salida.id(), salida.tourId(), salida.fecha(), salida.horaInicio(),
						salida.horaFin(), salida.cupoTotal(), cupoDisponible, salida.estado(), salida.guiaId(),
						salida.vehiculoId(), salida.bicicletaIds()));
			}
		}

		ReservaTour saved = new ReservaTour(reserva.id(), reserva.salidaId(), reserva.reservaHospedajeId(),
				reserva.clienteNombre(), reserva.pasajeros(), "CANCELADA");
		reservasTour.put(id, saved);
		return Optional.of(saved);
	}

	private static List<Long> safeIds(List<Long> ids) {
		return ids == null ? List.of() : List.copyOf(ids);
	}

	private static boolean boolOr(Boolean value, boolean fallback) {
		return value == null ? fallback : value;
	}

	private static int numberOr(Integer value, int fallback) {
		return value == null ? fallback : value;
	}

	private static BigDecimal moneyOr(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private static String textOr(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value;
	}
}

@RestController
@RequestMapping("/tours")
class TourController {

	private final OperacionesStore store;

	TourController(OperacionesStore store) {
		this.store = store;
	}

	@GetMapping
	List<Tour> all() {
		return store.findTours();
	}

	@GetMapping("/{id}")
	ResponseEntity<Tour> find(@PathVariable Long id) {
		return ResponseEntity.of(store.findTour(id));
	}

	@PostMapping
	ResponseEntity<Tour> create(@RequestBody Tour tour) {
		return ResponseEntity.status(HttpStatus.CREATED).body(store.createTour(tour));
	}

	@PutMapping("/{id}")
	ResponseEntity<Tour> update(@PathVariable Long id, @RequestBody Tour tour) {
		return ResponseEntity.of(store.updateTour(id, tour));
	}
}

@RestController
@RequestMapping("/salidas")
class SalidaController {

	private final OperacionesStore store;

	SalidaController(OperacionesStore store) {
		this.store = store;
	}

	@GetMapping
	List<SalidaTour> all(@RequestParam(required = false) String fecha) {
		return store.findSalidas(fecha);
	}

	@GetMapping("/{id}")
	ResponseEntity<SalidaTour> find(@PathVariable Long id) {
		return ResponseEntity.of(store.findSalida(id));
	}

	@PostMapping
	ResponseEntity<SalidaTour> create(@RequestBody SalidaTour salida) {
		return ResponseEntity.status(HttpStatus.CREATED).body(store.createSalida(salida));
	}

	@PostMapping("/{id}/cancelar")
	ResponseEntity<SalidaTour> cancelar(@PathVariable Long id) {
		return ResponseEntity.of(store.cancelarSalida(id));
	}

	@PostMapping("/{id}/asignar-guia")
	ResponseEntity<?> asignarGuia(@PathVariable Long id, @RequestBody AsignarGuiaRequest request) {
		return store.assignGuia(id, request.guiaId())
				.<ResponseEntity<?>>map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", "Salida o guia no encontrada")));
	}

	@PostMapping("/{id}/asignar-vehiculo")
	ResponseEntity<?> asignarVehiculo(@PathVariable Long id, @RequestBody AsignarVehiculoRequest request) {
		return store.assignVehiculo(id, request.vehiculoId())
				.<ResponseEntity<?>>map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", "Salida o vehiculo no encontrado")));
	}

	@PostMapping("/{id}/asignar-bicicleta")
	ResponseEntity<?> asignarBicicleta(@PathVariable Long id, @RequestBody AsignarBicicletaRequest request) {
		return store.assignBicicleta(id, request.bicicletaId())
				.<ResponseEntity<?>>map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", "Salida o bicicleta no encontrada")));
	}
}

@RestController
@RequestMapping("/guias")
class GuiaController {

	private final OperacionesStore store;

	GuiaController(OperacionesStore store) {
		this.store = store;
	}

	@GetMapping
	List<Guia> all() {
		return store.findGuias();
	}

	@PostMapping
	ResponseEntity<Guia> create(@RequestBody Guia guia) {
		return ResponseEntity.status(HttpStatus.CREATED).body(store.createGuia(guia));
	}

	@GetMapping("/{id}/agenda")
	ResponseEntity<?> agenda(@PathVariable Long id) {
		return store.findGuia(id)
				.<ResponseEntity<?>>map(guia -> ResponseEntity.ok(Map.of("guia", guia, "salidas", store.findAgendaGuia(id))))
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", "Guia no encontrada")));
	}
}

@RestController
@RequestMapping("/vehiculos")
class VehiculoController {

	private final OperacionesStore store;

	VehiculoController(OperacionesStore store) {
		this.store = store;
	}

	@GetMapping
	List<Vehiculo> all() {
		return store.findVehiculos();
	}

	@PostMapping
	ResponseEntity<Vehiculo> create(@RequestBody Vehiculo vehiculo) {
		return ResponseEntity.status(HttpStatus.CREATED).body(store.createVehiculo(vehiculo));
	}

	@PutMapping("/{id}")
	ResponseEntity<Vehiculo> update(@PathVariable Long id, @RequestBody Vehiculo vehiculo) {
		return ResponseEntity.of(store.updateVehiculo(id, vehiculo));
	}
}

@RestController
@RequestMapping("/bicicletas")
class BicicletaController {

	private final OperacionesStore store;

	BicicletaController(OperacionesStore store) {
		this.store = store;
	}

	@GetMapping
	List<Bicicleta> all() {
		return store.findBicicletas();
	}

	@PostMapping
	ResponseEntity<Bicicleta> create(@RequestBody Bicicleta bicicleta) {
		return ResponseEntity.status(HttpStatus.CREATED).body(store.createBicicleta(bicicleta));
	}

	@PutMapping("/{id}")
	ResponseEntity<Bicicleta> update(@PathVariable Long id, @RequestBody Bicicleta bicicleta) {
		return ResponseEntity.of(store.updateBicicleta(id, bicicleta));
	}
}

@RestController
@RequestMapping("/reservas-tour")
class ReservaTourController {

	private final OperacionesStore store;

	ReservaTourController(OperacionesStore store) {
		this.store = store;
	}

	@GetMapping
	List<ReservaTour> all(@RequestParam(required = false) Long reservaHospedajeId) {
		return store.findReservasTour(reservaHospedajeId);
	}

	@GetMapping("/{id}")
	ResponseEntity<ReservaTour> find(@PathVariable Long id) {
		return ResponseEntity.of(store.findReservaTour(id));
	}

	@PostMapping
	ResponseEntity<?> create(@RequestBody ReservaTour reserva) {
		return store.createReservaTour(reserva)
				.<ResponseEntity<?>>map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved))
				.orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", "No se pudo reservar la salida")));
	}

	@PostMapping("/{id}/cancelar")
	ResponseEntity<ReservaTour> cancelar(@PathVariable Long id) {
		return ResponseEntity.of(store.cancelarReservaTour(id));
	}
}
