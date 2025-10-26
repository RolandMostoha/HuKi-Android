package hu.mostoha.mobile.android.huki.data

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.domain.HikeRecommendation
import hu.mostoha.mobile.android.huki.model.domain.Landscape
import hu.mostoha.mobile.android.huki.model.domain.LandscapeType
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.TermeszetjaroTag

/**
 * Main landscapes of Hungary, focusing on the hiking related areas.
 *
 * It is a hardcoded data, coming from the Overpass query:
 * res/raw/landscapes_overpass_query.txt
 */
@Suppress("MagicNumber")
val LOCAL_LANDSCAPES = listOf(
    Landscape(
        osmId = "279573777",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_bukk,
        landscapeType = LandscapeType.MOUNTAIN_HIGH,
        color = R.color.color_landscape_bukk,
        center = Location(48.0356833, 20.5239573),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "bukk",
            HikeRecommendation.AKTIVKALANDOR to "bukk",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359977",
            areaName = "Bükkvidék (Észak-Magyarország, Magyarország)",
        ),
        destinations = DESTINATIONS_BUKK
    ),
    Landscape(
        osmId = "279665387",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_balaton_felvidék,
        landscapeType = LandscapeType.MOUNTAIN_WITH_LAKE,
        color = R.color.color_landscape_balaton_felvidék,
        center = Location(46.9174441, 17.7261084),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "balaton-felvidek",
            HikeRecommendation.AKTIVKALANDOR to "balaton-felvidek",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598301",
            areaName = "Balaton-felvidék (Hegycsoport)",
        ),
        destinations = DESTINATIONS_BALATON,
    ),
    Landscape(
        osmId = "279660398",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_aggteleki_karszt,
        landscapeType = LandscapeType.CAVE_SYSTEM,
        color = R.color.color_landscape_aggteleki_karszt,
        center = Location(48.4542508, 20.6350029),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "aggteleki-karszt",
            HikeRecommendation.AKTIVKALANDOR to "aggtelek-es-cserehat",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "32258172",
            areaName = "Aggteleki Nemzeti Park (Nemzeti Park)",
        ),
        destinations = DESTINATIONS_AGGTELEK,
    ),
    Landscape(
        osmId = "279665961",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_mecsek,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        color = R.color.color_landscape_mecsek,
        center = Location(46.1375511, 18.2469531),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "pecs-baranya",
            HikeRecommendation.AKTIVKALANDOR to "mecsek-villany",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23360001",
            areaName = "Mecsek vidéke (Dél-Dunántúl, Magyarország)",
        ),
        destinations = DESTINATIONS_MECSEK,
    ),
    Landscape(
        osmId = "279583932",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_matra,
        landscapeType = LandscapeType.MOUNTAIN_HIGH,
        color = R.color.color_landscape_matra,
        center = Location(47.8702858, 19.9453253),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "matra",
            HikeRecommendation.AKTIVKALANDOR to "matra",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359984",
            areaName = "Mátravidék (Észak-Magyarország, Magyarország)",
        ),
        destinations = DESTINATIONS_MATRA
    ),
    Landscape(
        osmId = "279564162",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_borzsony,
        landscapeType = LandscapeType.MOUNTAIN_HIGH,
        color = R.color.color_landscape_borzsony,
        center = Location(47.9128315, 18.9494417),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "dunakanyar",
            HikeRecommendation.AKTIVKALANDOR to "borzsony-es-dunakanyar",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359982",
            areaName = "Börzsönyvidék (Közép-Magyarország, Magyarország)",
        ),
        destinations = DESTINATIONS_BORZSONY,
    ),
    Landscape(
        osmId = "279561562",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_pilis_hegyseg,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        color = R.color.color_landscape_pilis_hegyseg,
        center = Location(47.6427423, 18.8986191),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "budapest-kornyeke",
            HikeRecommendation.AKTIVKALANDOR to "pilis-es-visegradi-hegyseg",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598246",
            areaName = "Pilis-hegység (Hegycsoport)",
        ),
        destinations = DESTINATIONS_PILIS,
    ),
    Landscape(
        osmId = "279561563",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_visegradi_hegyseg,
        landscapeType = LandscapeType.MOUNTAIN_WITH_CASTLE,
        color = R.color.color_landscape_visegradi_hegyseg,
        center = Location(47.7210692, 18.9181598),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "dunakanyar",
            HikeRecommendation.AKTIVKALANDOR to "pilis-es-visegradi-hegyseg",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359987",
            areaName = "Visegrádi-hegység (Közép-Magyarország, Magyarország)",
        ),
        destinations = DESTINATIONS_VISGERAD,
    ),
    Landscape(
        osmId = "279665156",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_bakony,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        color = R.color.color_landscape_bakony,
        center = Location(47.1624906, 17.7835194),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "bakony-veszprem",
            HikeRecommendation.AKTIVKALANDOR to "bakony",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359991",
            areaName = "Bakonyvidék (Közép-Dunántúl, Magyarország)",
        ),
        destinations = DESTINATIONS_BAKONY,
    ),
    Landscape(
        osmId = "279663379",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_gerecse_hegyseg,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        color = R.color.color_landscape_gerecse_hegyseg,
        center = Location(47.6177834, 18.5489089),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "vertes-gerecse-velencei-to",
            HikeRecommendation.AKTIVKALANDOR to "vertes-gerecse",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598247",
            areaName = "Gerecse (Hegycsoport)",
        ),
        destinations = DESTINATIONS_GERECSE,
    ),
    Landscape(
        osmId = "279665573",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_keszthelyi_hegyseg,
        landscapeType = LandscapeType.MOUNTAIN_WITH_LAKE,
        color = R.color.color_landscape_keszthelyi_hegyseg,
        center = Location(46.8503130, 17.2709995),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "keszthely-es-kornyeke",
            HikeRecommendation.AKTIVKALANDOR to "kis-balaton",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598291",
            areaName = "Keszthelyi-hegység (Hegycsoport)",
        ),
        destinations = DESTINATIONS_KESZTHELY,
    ),
    Landscape(
        osmId = "279590728",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_cserhat,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        color = R.color.color_landscape_cserhat,
        center = Location(47.8688179, 19.4148133),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "palocfold",
            HikeRecommendation.AKTIVKALANDOR to "cserhat",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359979",
            areaName = "Cserhátvidék (Észak-Magyarország, Magyarország)",
        ),
        destinations = DESTINATIONS_CSERHAT,
    ),
    Landscape(
        osmId = "279656184",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_zempleni_hegyseg,
        landscapeType = LandscapeType.WINE_AREA,
        color = R.color.color_landscape_zempleni_hegyseg,
        center = Location(48.3440651, 21.4169262),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "zemplen",
            HikeRecommendation.AKTIVKALANDOR to "zemplen",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "25014010",
            areaName = "Zempléni Tájvédelmi Körzet (Tájvédelmi körzet)",
        ),
        destinations = DESTINATIONS_ZEMPLEN,
    ),
    Landscape(
        osmId = "279663918",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_vertes,
        landscapeType = LandscapeType.MOUNTAIN_LOW,
        color = R.color.color_landscape_vertes,
        center = Location(47.4175701, 18.3625658),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "vertes-gerecse-velencei-to",
            HikeRecommendation.AKTIVKALANDOR to "vertes-gerecse",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359992",
            areaName = "Vértes és vidéke (Közép-Dunántúl, Magyarország)",
        ),
        destinations = DESTINATIONS_VERTES,
    ),
    Landscape(
        osmId = "279664160",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_velencei_hegyseg,
        landscapeType = LandscapeType.MOUNTAIN_WITH_LAKE,
        color = R.color.color_landscape_velencei_hegyseg,
        center = Location(47.2654220, 18.5854126),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "vertes-gerecse-velencei-to",
            HikeRecommendation.AKTIVKALANDOR to "velencei-to-es-volgyvidek",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "59627042",
            areaName = "Velencei-hegység (Közép-Dunántúl, Magyarország)",
        ),
        destinations = DESTINATIONS_VELENCE,
    ),
    Landscape(
        osmId = "279666014",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_zselic,
        landscapeType = LandscapeType.STAR_GAZING_AREA,
        color = R.color.color_landscape_zselic,
        center = Location(46.2185793, 17.8800546),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "tolna",
            HikeRecommendation.AKTIVKALANDOR to "zselic",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "25014012",
            areaName = "Zselici Tájvédelmi Körzet (Tájvédelmi körzet)",
        ),
        destinations = DESTINATIONS_ZSELIC,
    ),
    Landscape(
        osmId = "279667079",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_villanyi_hegyseg,
        landscapeType = LandscapeType.WINE_AREA,
        color = R.color.color_landscape_villanyi_hegyseg,
        center = Location(45.8747512, 18.2730710),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "pecs-baranya",
            HikeRecommendation.AKTIVKALANDOR to "mecsek-villany",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598286",
            areaName = "Villányi-hegység (Hegycsoport)",
        ),
        destinations = DESTINATIONS_VILLANY,
    ),
    Landscape(
        osmId = "300323308",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_koszegi_hegyseg,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        color = R.color.color_landscape_koszegi_hegyseg,
        center = Location(47.3307697, 16.4054968),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "koszeg-es-szombathely-kornyeke",
            HikeRecommendation.AKTIVKALANDOR to "koszegi-hegyseg",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "25013914",
            areaName = "Kőszegi Tájvédelmi Körzet (Tájvédelmi körzet)",
        ),
        destinations = DESTINATIONS_KOSZEG
    ),
    Landscape(
        osmId = "279593581",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_heves_borsodi_dombsag,
        landscapeType = LandscapeType.MOUNTAIN_LOW,
        color = R.color.color_landscape_heves_borsodi_dombsag,
        center = Location(48.1120709, 20.1976971),
        areaTags = emptyMap(),
        termeszetjaroTag = null,
        destinations = DESTINATIONS_HEVES,
    ),
    Landscape(
        osmId = "3716160",
        osmType = PlaceType.RELATION,
        nameRes = R.string.landscape_budai_hegyseg,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        color = R.color.color_landscape_budai_hegyseg,
        center = Location(47.5428510, 18.9236294),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "budapest",
            HikeRecommendation.AKTIVKALANDOR to "fovaros-es-kornyeke/budapest-es-agglomeracio/",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598254",
            areaName = "Budai-hegység (Hegycsoport)",
        ),
        destinations = DESTINATIONS_BUDAPEST,
    ),
    Landscape(
        osmId = "11073175",
        osmType = PlaceType.RELATION,
        nameRes = R.string.landscape_soproni_hegyseg,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        color = R.color.color_landscape_soproni_hegyseg,
        center = Location(47.6438256, 16.4890472),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "sopron-es-kornyeke",
            HikeRecommendation.AKTIVKALANDOR to "soproni-hegyseg-es-ferto-to",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598250",
            areaName = "Soproni-hegység (Hegyvonulat)",
        ),
        destinations = DESTINATIONS_SOPRON,
    ),
    Landscape(
        osmId = "6503266",
        osmType = PlaceType.RELATION,
        nameRes = R.string.landscape_hortobagy,
        landscapeType = LandscapeType.PLAIN_LAND,
        color = R.color.color_landscape_hortobagy,
        center = Location(47.49350, 21.05344),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "hortobagy-tisza-to-debrecen",
            HikeRecommendation.AKTIVKALANDOR to "tisza-to-es-hortobagy",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "32258164",
            areaName = "Hortobágyi Nemzeti Park (Nemzeti Park)",
        ),
        destinations = DESTINATIONS_HORTOBAGY,
    ),
    Landscape(
        osmId = "8545511",
        osmType = PlaceType.RELATION,
        nameRes = R.string.landscape_orseg,
        landscapeType = LandscapeType.PLAIN_LAND,
        color = R.color.color_landscape_orseg,
        center = Location(46.83921, 16.40093),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "orseg",
            HikeRecommendation.AKTIVKALANDOR to "orseg-es-vendvidek",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "32258166",
            areaName = "Őrségi Nemzeti Park (Nemzeti Park)",
        ),
        destinations = DESTINATIONS_ORSEG,
    ),
    Landscape(
        osmId = "22719",
        osmType = PlaceType.RELATION,
        nameRes = R.string.landscape_alfold,
        landscapeType = LandscapeType.PLAIN_LAND,
        color = R.color.color_landscape_alfold,
        center = Location(latitude = 46.55921, longitude = 20.19168),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "del-alfold",
            HikeRecommendation.AKTIVKALANDOR to "del-alfold",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23406038",
            areaName = "Dél-Alföld (Magyarország)",
        ),
        destinations = DESTINATIONS_ALFOLD,
    ),
    Landscape(
        osmId = "380535667",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_zalai_dombsag,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        color = R.color.color_landscape_zalai_dombsag,
        center = Location(latitude = 46.7060, longitude = 16.7191),
        areaTags = mapOf(
            HikeRecommendation.KIRANDULASTIPPEK to "gocsej-zala",
            HikeRecommendation.AKTIVKALANDOR to "zalai-dombsag",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359999",
            areaName = "Zalai-dombság (Nyugat-Dunántúl, Magyarország)",
        ),
        destinations = DESTINATIONS_ZALA,
    ),
    Landscape(
        osmId = "380535854",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_szigetkoz,
        landscapeType = LandscapeType.LAKE,
        color = R.color.color_landscape_szigetkoz,
        center = Location(latitude = 47.8615, longitude = 17.4010),
        areaTags = mapOf(
            HikeRecommendation.AKTIVKALANDOR to "szigetkoz-es-pannonhalma",
        ),
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "22729870",
            areaName = "Szigetközi Tájvédelmi Körzet (Tájvédelmi körzet)",
        ),
        destinations = DESTINATIONS_SZIGETKOZ,
    ),
)
