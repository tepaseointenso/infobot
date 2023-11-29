package com.seotepa.infobotApp

data class CarreraInformatica(
    val id: String,
    val nombre: String,
    val grado: String,
    val titulo: String,
    val image: Int,
    val duracionSemestres: Int,
    val codigo: Int,
    val malla: String,
    val ponderacion: Map<String, Int>,
    val puntajeReferencia: Map<String, Double>,
    val resenaCarrera: String,
    val campoOcupacional: String,
    val perfilEgreso: List<String>
)

data class Diplomado(
    val id: String,
    val title: String,
    val duracion: String,
    val image: Int,
    val fechas: Pair<String, String>,
    val horario: Pair<String, String>,
    val arancel: Int,
    val modalidad: String,
    val lugar: String,
    val descripcion: String,
    val objetivos: List<String>? = null,
    val modulos: List<String>,
    val profesores: List<String>,
    val dirigidoA: List<String>? = null,
    val requisitos: String? = null,
    val formasDePago: List<String>? = null,
    val descuentos: List<String>? = null,
    val documentosDePostulacion: List<String>? = null
)

val listaCarreras = listOf(
    CarreraInformatica(
        id = "1",
        nombre = "Ingeniería en Informática",
        grado = "Licenciado en Ingeniería",
        image = R.drawable.informatica,
        titulo = "Ingeniero en Informática",
        duracionSemestres = 8,
        codigo = 14104,
        malla = "https://www.inf.ucv.cl/wp-content/uploads/2021/10/ingenieria_en_informatica.pdf",
        ponderacion = mapOf(
        "NEM" to 20,
        "Ranking" to 20,
        "ComprensionLectora" to 20,
        "Matematicas" to 30,
        "CienciasHistoria" to 10
        ),
        puntajeReferencia = mapOf(
        "Puntaje ponderado mínimo de postulación" to 500.0,
        "Puntaje promedio (C.lectora y Matemática)" to 475.0
        ),
        resenaCarrera = "El/la ingeniero/a en informática de la Pontificia Universidad Católica de Valparaíso, es un/a profesional con un marcado sello valórico dispuesto/a a contribuir al desarrollo y al bienestar social mediante el ejercicio de su profesión. Para garantizar el eficiente e íntegro desempeño de las funciones asociadas a la labor ingenieril posee una significativa formación en ciencias básicas, ciencias de la ingeniería e ingeniería aplicada.\n\nSu formación integral basada en un conjunto sistemático de saberes cognitivos, procedimentales y actitudinales, le permite hacer un correcto uso de las nuevas tecnologías y entregar soluciones eficientes y de calidad relacionadas con el desarrollo, operación y mantenimiento de sistemas informáticos. Se caracteriza por su capacidad de aprendizaje autónomo y flexibilidad para trabajar en proyectos con equipos interdisciplinarios, con un enfoque centrado en el usuario.",
        campoOcupacional = "El campo laboral del Ingeniero(a) en Informática lo habilita para:\n\nDesempeñarse en diversas empresas o instituciones que utilicen herramientas tecnológicas para el desarrollo o comercialización de productos de software, en áreas como: educación, salud, seguros, minería, financiera, transporte, comunicaciones, industria y gobierno, entre otras.\nHacerse cargo del desarrollo de aplicaciones informáticas, identificando herramientas y plataformas tecnológicas adecuadas.\nPuede desenvolverse como administrador de sistemas y productos informáticos, así como también trabajar en empresas de servicios relacionados al área.\nEjercer sus labores en unidades de informática en empresas productoras de bienes y servicios del área, tendiendo las capacidades se ser parte de éstas hasta llegar a dirigirlas.\nParticipar en proyectos interdisciplinarios y desenvolverse como consultor en el desarrollo de estudios y asesorías, en el ámbito de su especialidad.\nDesempeñarse de manera independiente como prestador de servicios especializados a distintos tipos de organizaciones, tanto del sector público como privado.",
        perfilEgreso = listOf(
        "Reconoce la dimensión trascendente de la existencia humana y la antropología cristiana como respuesta valiosa al sentido de la vida.",
        "Actúa éticamente, iluminado/a por la propuesta cristiana, en contextos reales, con autonomía y respeto hacia los/las demás, buscando el bien común, la promoción de los derechos humanos y la realización de la persona humana en un contexto de diversidad.",
        // ... (otras competencias de formación fundamental)
        )
        ),
    CarreraInformatica(
        id = "2",
        nombre = "Ingeniería Civil Informática",
        grado = "Licenciado en Ciencias de la Ingeniería",
        image = R.drawable.civil,
        titulo = "Ingeniero Civil en Informática",
        duracionSemestres = 11,
        codigo = 14064,
        malla = "https://www.inf.ucv.cl/wp-content/uploads/2020/08/pe_ingenier__a_civil_infirm__tica.pdf",
        ponderacion = mapOf(
        "NEM" to 20,
        "Ranking" to 20,
        "ComprensionLectora" to 20,
        "Matematicas" to 30,
        "CienciasHistoria" to 10
        ),
        puntajeReferencia = mapOf(
        "PrimerSeleccionado" to 771.3,
        "UltimoSeleccionado" to 600.8
        ),
        resenaCarrera = "El/la Ingeniero/a Civil en Informática de la Pontificia Universidad Católica de Valparaíso, es un/a profesional con un marcado sello valórico y socialmente responsable. Posee una amplia formación en ciencias básicas, ciencias de la ingeniería e ingeniería aplicada, que lo habilita para resolver problemas de procesamiento de datos y generación de información. Es capaz de concebir y establecer soluciones eficientes e innovadoras como respuesta a las necesidades organizacionales, en el ámbito de su especialidad.\n\nSu formación integral le permite gestionar proyectos informáticos, caracterizándose por su capacidad de liderazgo, aprendizaje y adaptabilidad a equipos de trabajo multidisciplinarios.",
        campoOcupacional = "En empresas productivas tales como: de procesos industriales, de diseño de sistemas electrónicos, de manufactura, de la minería, etc. En ellas la actividad del profesional se centra en la: gestión, planificación, diseño, puesta en marcha, supervisión y explotación de los sistemas y procesos industriales.",
        perfilEgreso = listOf(
        "Reconoce la dimensión trascendente de la existencia humana, y la antropología cristiana como respuesta valiosa al sentido de la vida.",
        "Actúa éticamente, iluminado/a por la propuesta cristiana, en contextos reales, con autonomía y respeto hacia los demás, buscando el bien común, la promoción de los derechos humanos y la realización de la persona humana, en un contexto de diversidad.",
        // ... (otras competencias de formación fundamental)
        ),
    ),
    CarreraInformatica(
        id = "3",
        nombre = "Ingeniería Civil en Ciencia de Datos",
        grado = "Licenciado/a en Ciencias de la Ingeniería",
        titulo = "Ingeniero/a Civil en Ciencia de Datos",
        image = R.drawable.datos,
        duracionSemestres = 11,
        codigo = 14100,
        malla = "https://www.inf.ucv.cl/wp-content/uploads/2020/08/pe_ingenier__a_civil_en_ciencia_de_datos.pdf",
        ponderacion = mapOf(
        "NEM" to 20,
        "Ranking" to 20,
        "ComprensionLectora" to 20,
        "Matematicas" to 30,
        "CienciasHistoria" to 10
        ),
        puntajeReferencia = mapOf(
        "PrimerSeleccionado" to 681.0,
        "UltimoSeleccionado" to 521.9
        ),
        resenaCarrera = "El/la Ingeniero/a Civil en Ciencia de Datos de la Pontificia Universidad Católica de Valparaíso, es un/a profesional con un sello valórico dispuesto a contribuir al desarrollo y bienestar social mediante el ejercicio de su profesión. Para garantizar el eficiente e íntegro desempeño de las funciones asociadas a la labor ingenieril posee una amplia formación en Ciencias Básicas, Ciencias de la Ingeniería e Ingeniería Aplicada.\n\nSu formación integral le permite gestionar proyectos tecnológicos enfocados en la extracción, transformación y generación de conocimiento basado en volúmenes de datos de pequeña y gran escala. Posee capacidades de modelamiento matemático y estadístico, utiliza y/o desarrolla tecnologías de la información con visión de negocio. Se caracteriza por su capacidad de liderazgo, aprendizaje y adaptabilidad a equipos de trabajo interdisciplinarios.",
        campoOcupacional = "El campo laboral del Ingeniero/a en Ciencia de Datos lo habilita para concebir y gestionar proyectos de ciencia de datos, recolectar, procesar y explorar datos, construir y validar modelos de ciencia de datos, visualizar y comunicar resultados, y aplicar medidas que aseguren la privacidad y protección de datos personales.",
        perfilEgreso = listOf(
        "Reconoce la dimensión trascendente de la existencia humana, y la antropología cristiana como respuesta valiosa al sentido de la vida.",
        "Actúa éticamente, iluminado por la propuesta cristiana, en contextos reales, con autonomía y respeto hacia los demás, buscando el bien común, la promoción de los derechos humanos y la realización de la persona humana, en un contexto de diversidad.",
        // ... (otras competencias de formación fundamental)
        ),
        ),
    CarreraInformatica(
        id = "4",
        nombre = "Ingeniería de Ejecución en Informática",
        grado = "No aplica",
        titulo = "Ingeniero/a de Ejecución en Informática",
        image = R.drawable.ejecucion,
        duracionSemestres = 8,
        codigo = 14074,
        malla = "http://www.inf.ucv.cl/wp-content/uploads/2022/03/pe_ingenier__a_de_ejecuci__n_en_inform__tica-2-1.png",
        ponderacion = mapOf(
        "NEM" to 20,
        "Ranking" to 20,
        "ComprensionLectora" to 20,
        "Matematicas" to 30,
        "CienciasHistoria" to 10
        ),
        puntajeReferencia = emptyMap(),  // No hay puntajes de referencia para carreras sin admisión
        resenaCarrera = "El/la Ingeniero/a de Ejecución en Informática de la Pontificia Universidad Católica de Valparaíso, es un/a profesional con un marcado sello valórico y socialmente responsable.\n\nPosee formación en ciencias básicas, ciencias de la ingeniería e ingeniería aplicada, que lo/la habilita para participar en el desarrollo, operación y mantenimiento de sistemas informáticos.\n\nParticipa en la ejecución de proyectos informáticos utilizando eficientemente las herramientas tecnológicas disponibles, caracterizándose por su capacidad de aprendizaje y adaptabilidad a equipos de trabajo multidisciplinarios.",
        campoOcupacional = "Desempeñarse en diversas empresas o instituciones que utilicen herramientas tecnológicas para el desarrollo o comercialización de productos de software, en áreas como: educación, salud, seguros, minería, financiera, transporte, comunicaciones, industria y gobierno, entre otras.",
        perfilEgreso = listOf(
        "Reconoce la dimensión trascendente de la existencia humana, y la antropología cristiana como respuesta valiosa al sentido de la vida.",
        "Actúa éticamente, iluminado/a por la propuesta cristiana, en contextos reales, con autonomía y respeto hacia los demás, buscando el bien común, la promoción de los derechos humanos y la realización de la persona humana, en un contexto de diversidad.",
        // ... (otras competencias de formación fundamental)
        ),
        )
)

val listaDiplomados = listOf(Diplomado(
    id = "1",
    title = "Diplomado en Big Data y Data Science",
    duracion = "4 meses 81 horas",
    image = R.drawable.diplomado_bigdata,
    fechas = Pair("1 de septiembre 2023", "22 de diciembre de 2023"),
    horario = Pair("Viernes 19:00 a 22:15", "Sábado 10:00 a 13:15"),
    arancel = 1700000,
    modalidad = "Online síncrona (Presencial)",
    lugar = "1er semestre CEA Stgo.\n2do semestre IBC Valparaíso",
    descripcion = "El Diplomado en Big Data y Data Science está dirigido a profesionales del área informática o afín que requieran utilizar herramientas computacionales y estadísticas para procesar y analizar grandes volúmenes de datos.",
    objetivos = listOf(
        "Comprender el contexto y las tendencias que permiten generar conocimiento explícito sobre la base del almacenamiento de grandes volúmenes de datos.",
        "Comprender los problemas asociados al procesamiento y análisis de grandes volúmenes de datos.",
        "Conocer y experimentar con modelos, algoritmos y herramientas asociados al procesamiento y análisis de grandes volúmenes de datos.",
        "Desarrollar aplicaciones que procesan y analizan grandes volúmenes de datos."
    ),
    modulos = listOf(
        "MÓDULO 1: Datos como fuente de valor",
        "MÓDULO 2: Big Data",
        "MÓDULO 3: Data Science"
    ),
    profesores = listOf(
        "Dr. Rodrigo Alfaro - Doctor en Ingeniería Informática, Universidad Técnica Federico Santa María",
        "Dr. Héctor Allende Cid - Doctor en Ingeniería Informática, Universidad Técnica Federico Santa María",
        "Dr. Wenceslao Palma - Doctor en Informática, Universidad de Nantes"
    )
),
    Diplomado(
        id = "2",
        title = "Diplomado en Inteligencia Artificial",
        duracion = "4 meses 81 horas",
        image = R.drawable.diplomado_ai,
        fechas = Pair("04 agosto 2023", "25 de noviembre 2023"),
        horario = Pair("Viernes de 19:00 a 22:15", "Sábado de 10:00 a 13:15"),
        arancel = 1500000,
        modalidad = "100% virtual (online, clases síncronas)",
        lugar = "Zoom",
        descripcion = "El Diplomado de Inteligencia Artificial de Escuela de Informática de la PUCV está dirigido a profesionales que desean potenciar su desarrollo profesional especializándose en el uso de la Inteligencia Artificial como herramienta para desarrollar soluciones tecnológicas innovadoras. El programa ha sido diseñado para entregar un conocimiento amplio sobre las técnicas disponibles en la actualidad y su uso, abordando las posibilidades que la Inteligencia Artificial ofrece hoy y en el futuro.",
        objetivos = listOf(
            "Conocer el contexto y las tendencias de la automatización basada en Inteligencia Artificial.",
            "Comprender las bases fundamentales de la Inteligencia Artificial y sus aplicaciones.",
            "Conocer y experimentar con modelos, algoritmos y herramientas basadas en la Inteligencia Artificial.",
            "Desarrollar prototipos que planteen soluciones basadas en Inteligencia Artificial."
        ),
        modulos = listOf(
            "MÓDULO 1: Inteligencia y conocimiento como fuente de valor",
            "MÓDULO 2: MÓDULO 3: Inteligencia Artificial en Optimización",
            "MÓDULO 4: Módulo 4: Aprendizaje Automático"
        ),
        profesores = listOf(
            "Dr. Héctor Allende-Cid - Doctor en Ingeniería Informática, Universidad Técnica Federico Santa María, Chile.",
            "Dr. Rodrigo Alfaro - Doctor en Ingeniería Informática, Universidad Técnica Federico Santa María, Chile.",
            "Dra. Leslie Pérez - Dra. en Cs de la Ingeniería y Tecnología, Universidad Libre de Bruselas, Bélgica.",
            "Dr. Broderick Crawford - Doctor en Ingeniería Informática, Universidad Técnica Federico Santa María, Chile.",
            "Dr. Claudio Cubillos - Doctor en Ingeniería de Sistemas, Politécnico de Torino, Italia.",
            "Dr. Ricardo Soto - Doctor en Informática, Universidad de Nantes, Francia.",
            "MSc. Silvana Roncagliolo - MSc. en Ciencia de la Computación, Universidad Estatal de Oregon, Estados Unidos.",
            "Dr. Gonzalo Farías - Doctor en Ingeniería Informática, Universidad Complutense de Madrid, España."
        )
    ),
    Diplomado(
        id= "3",
        title = "Diplomado en Experiencia del Usuario",
        duracion = "4 meses 75 horas",
        image = R.drawable.diplomado_ux,
        fechas = Pair("28 Agosto 2023", "18 Diciembre 2023"),
        horario = Pair("Lunes 19:00 a 22:15", "Miercoles 19:00 a 22:15"),
        arancel = 1500000,
        modalidad = "100% virtual (online, clases síncronas)",
        lugar = "Online",
        descripcion = "El Diplomado en Experiencia del Usuario pretende desarrollar habilidades para identificar las necesidades reales de los usuarios, diseñar y evaluar la experiencia del usuario. El programa se dicta desde 2015, pero su nueva versión fue rediseñada, en cuanto a duración, contenido y modalidad. El programa se dicta 100% online y está abierto a postulantes chilenos y extranjeros.",
        modulos = listOf(
            "MÓDULO 1: Introducción",
            "MÓDULO 2: Psicología aplicada a la experiencia del usuario",
            "MÓDULO 3: Aspectos legales y éticos en la experiencia del usuario",
            "MÓDULO 4: La evaluación de la experiencia del usuario",
            "MÓDULO 5: El diseño de la experiencia del usuario",
            "MÓDULO 6: Diseño emocional y accesible"
        ),
        profesores = listOf(
            "Dr. Cristian Rusu - Doctor en Ciencias de la Ingeniería. Technical University of Cluj – Napoca, Rumania.",
            "Dra. Sandra Cano - Doctora en Ciencias de la Electrónica, Universidad del Cauca, Colombia.",
            "Dr(c). Nicolás Matus - Candidato a Doctor en Ingeniería Informática, Pontificia Universidad Católica de Valparaíso.",
            "Relatores Invitados: Alejandro Schleyer (Abogado), Virginia Rusu (Psicóloga, Arquitecta), Katherine Durán (Diseñadora, Mag. en Ing. Informática), Jaime Díaz (Dr. en Ing. Informática), Tamara Reyes (Diseñadora), Matilde Estrada (Diseñadora)"
        ),
        dirigidoA = listOf(
            "Profesionales de cualquier especialidad que requieren desarrollar habilidades de diseño y evaluación de la Experiencia del Usuario.",
            "Diseñadores de Experiencia del Usuario y asesores en Experiencia del Usuario de productos, sistemas, servicios.",
            "Participantes en procesos específicos del área Experiencia del Usuario, en entidades públicas o privadas."
        ),
        requisitos = "Si bien no hay conocimiento, habilidades, destrezas preestablecidas, la pertenencia al público objetivo se comprueba por la revisión de los documentos de postulación: CV y certificados de título y/o grado.",
        formasDePago = listOf(
            "Transferencia electrónica",
            "Tarjetas de débito/crédito",
            "Cheques. Se puede pagar en hasta 4 cuotas de igual valor."
        ),
        descuentos = listOf(
            "20% descuento pago anticipado/contado (acumulativo con otro descuento).",
            "25% descuento ex estudiantes PUCV.",
            "40% descuento estudiantes PUCV.",
            "30% descuento profesores y funcionarios PUCV.",
            "30% descuento empresas desde 2 inscritos."
        ),
        documentosDePostulacion = listOf(
            "Certificado de nacimiento",
            "CV",
            "Certificados de título técnico/profesional y/o grado."
        )
    ),
    Diplomado(
        id= "4",
        title = "Diplomado en Experiencia del Consumidor",
        duracion = "4 meses - 75 horas",
        image = R.drawable.diplomado_consumer,
        fechas = Pair("25 abril 2022", "20 julio 2022"),
        horario = Pair("Lunes 19:00 a 22:15", "Miercoles 19:00 a 22:15"),
        arancel = 1500000,
        modalidad = "100% virtual (online, clases síncronas)",
        lugar = "Online",
        descripcion = "El Diplomado en Experiencia del Consumidor pretende desarrollar habilidades necesarias para identificar las necesidades reales de los consumidores, diseñar y evaluar la experiencia del consumidor. El programa se dicta 100% online y está abierto a postulantes chilenos y extranjeros.",
        objetivos = null,
        modulos = listOf(
            "MÓDULO 1: Introducción",
            "MÓDULO 2: Consumidores y necesidades",
            "MÓDULO 3: El viaje del consumidor",
            "MÓDULO 4: El diseño y la evaluación de la experiencia del consumidor",
            "MÓDULO 5:La experiencia del consumidor y el marketing",
            "MÓDULO 6: Psicología aplicada a la experiencia del consumidor",
            "MÓDULO 7: Aspectos legales y éticos en la experiencia del consumidor"
        ),
        profesores = listOf(
            "Dr. Cristian Rusu",
            "Dra. Daniela Quiñones Otey",
            "Dra. Sandra Cano",
            "Relatores invitados"
        ),
        dirigidoA = listOf(
            "Profesionales de cualquier especialidad que requieren desarrollar habilidades de diseño y evaluación de la Experiencia del Consumidor.",
            "Diseñadores de Experiencia del Consumidor y Experiencia del Usuario, y asesores en Experiencia del Consumidor, Experiencia del Usuario, Servicios.",
            "Participantes en procesos específicos del área Experiencia del Consumidor, en entidades públicas o privadas."
        ),
        requisitos = "Si bien no hay conocimiento, habilidades, destrezas preestablecidas, la pertenencia al público objetivo se comprueba por la revisión de los documentos de postulación: CV y certificados de título y/o grado.",
        formasDePago = listOf("Transferencia electrónica", "Tarjetas de débito/crédito"),
        descuentos = listOf("Descuentos disponibles"),
        documentosDePostulacion = listOf("Certificado de nacimiento", "CV", "Certificados de título técnico/profesional y/o grado."),
    ),
    Diplomado(
        id= "5",
        title = "Diplomado en Ciberseguridad",
        duracion = "90 horas",
        image = R.drawable.diplomado_cyber,
        fechas = Pair("1 de septiembre 2023", "13 de enero 2024"),
        horario = Pair("Viernes 19:00 hrs a 22:15", "Sábado 10:00 a 13:15"),
        arancel = 1550000,
        modalidad = "100% virtual (online, clases síncronas)",
        lugar = "Online",
        descripcion = "El diplomado en Ciberseguridad entrega los conocimientos teóricos y prácticos para gestionar de manera efectiva la comunicación de aspectos de ciberseguridad en una organización, comprender los requerimientos éticos, legales y técnicos para generar soluciones de ciberseguridad así como también dirigir proyectos de desarrollo o integración de sistemas o aplicaciones seguras.",
        objetivos = null,
        modulos = listOf(
            "MÓDULO 1 - GESTIÓN DE LA CIBERSEGURIDAD EN LA ORGANIZACIÓN 18 hrs",
            "MÓDULO 2 - Desarrollo de aplicaciones seguras 24 hrs",
            "MÓDULO 3 - PENETRATION TESTING. 24 hrs",
            "MÓDULO 4 - Investigación forense 24 hrs"
        ),
        profesores = listOf(
            "Héctor Gómez",
            "Iván Mercado",
            "Sebastián Berrios",
            "Pablo Itaim",
            "Wenceslao Palma Muñoz"
        ),
        dirigidoA = listOf(
            "Profesionales de todos los sectores de la industria, que estén trabajando o sean responsables de la seguridad de la información en una empresa, que dirijan proyectos de desarrollo de sistemas o aplicaciones seguras y profesionales interesados en el área."
        ),
        requisitos = "Conocimientos previos deseables: programación, redes de computadores.",
        formasDePago = listOf("Tarjetas de débito/crédito (botón de pago)"),
        descuentos = listOf("25% ex alumnos PUCV", "25% empresas (a partir de 2 inscritos)", "30% funcionarios PUCV"),
        documentosDePostulacion = listOf("CV")
    )
)