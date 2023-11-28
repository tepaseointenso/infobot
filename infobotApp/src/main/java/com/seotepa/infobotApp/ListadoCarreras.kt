package com.seotepa.infobotApp

data class CarreraInformatica(
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

val listaCarreras = listOf(
    CarreraInformatica(
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