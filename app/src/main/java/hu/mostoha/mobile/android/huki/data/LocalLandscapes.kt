package hu.mostoha.mobile.android.huki.data

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.domain.Landscape
import hu.mostoha.mobile.android.huki.model.domain.LandscapeType
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.TermeszetjaroTag

/**
 * Main landscapes of Hungary, focusing on the hiking related areas.
 *
 * It is a hardcoded data, coming from the following Overpass query:
 * way["natural"="mountain_range"](45.75948,16.20229,48.62385,22.71053);
 * relation["natural"="mountain_range"](45.75948,16.20229,48.62385,22.71053);
 */
@Suppress("MagicNumber")
val LOCAL_LANDSCAPES = listOf(
    Landscape(
        osmId = "3716160",
        osmType = PlaceType.RELATION,
        nameRes = R.string.landscape_budai_hegyseg,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        center = Location(47.5428510, 18.9336294),
        kirandulastippekTag = "budapest",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598254",
            areaName = "Budai-hegység (Hegycsoport)",
        ),
    ),
    Landscape(
        osmId = "279573777",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_bükk,
        landscapeType = LandscapeType.MOUNTAIN_HIGH,
        center = Location(48.0356833, 20.5239573),
        kirandulastippekTag = "bukk",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359977",
            areaName = "Bükkvidék (Észak-Magyarország, Magyarország)",
        ),
    ),
    Landscape(
        osmId = "279665387",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_balaton_felvidék,
        landscapeType = LandscapeType.MOUNTAIN_WITH_LAKE,
        center = Location(46.9474441, 17.7261084),
        kirandulastippekTag = "balaton-felvidek",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598301",
            areaName = "Balaton-felvidék (Hegycsoport)",
        ),
    ),
    Landscape(
        osmId = "279660398",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_aggteleki_karszt,
        landscapeType = LandscapeType.CAVE_SYSTEM,
        center = Location(48.4542508, 20.6350029),
        kirandulastippekTag = "aggteleki-karszt",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "32258172",
            areaName = "Aggteleki Nemzeti Park (Nemzeti Park)",
        ),
    ),
    Landscape(
        osmId = "279665961",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_mecsek,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        center = Location(46.1675511, 18.2469531),
        kirandulastippekTag = "pecs-baranya",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23360001",
            areaName = "Mecsek vidéke (Dél-Dunántúl, Magyarország)",
        ),
    ),
    Landscape(
        osmId = "279583932",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_mátra,
        landscapeType = LandscapeType.MOUNTAIN_HIGH,
        center = Location(47.8902858, 19.9453253),
        kirandulastippekTag = "matra",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359984",
            areaName = "Mátravidék (Észak-Magyarország, Magyarország)",
        ),
    ),
    Landscape(
        osmId = "279564162",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_börzsöny,
        landscapeType = LandscapeType.MOUNTAIN_HIGH,
        center = Location(47.9128315, 18.9494417),
        kirandulastippekTag = "dunakanyar",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359982",
            areaName = "Börzsönyvidék (Közép-Magyarország, Magyarország)",
        ),
    ),
    Landscape(
        osmId = "279561562",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_pilis_hegység,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        center = Location(47.6627423, 18.8986191),
        kirandulastippekTag = "budapest-kornyeke",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598246",
            areaName = "Pilis-hegység (Hegycsoport)",
        ),
    ),
    Landscape(
        osmId = "279561563",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_visegrádi_hegység,
        landscapeType = LandscapeType.MOUNTAIN_WITH_CASTLE,
        center = Location(47.7320692, 18.9181598),
        kirandulastippekTag = "dunakanyar",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359987",
            areaName = "Visegrádi-hegység (Közép-Magyarország, Magyarország)",
        ),
    ),
    Landscape(
        osmId = "279665156",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_bakony,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        center = Location(47.1624906, 17.7835194),
        kirandulastippekTag = "bakony-veszprem",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359991",
            areaName = "Bakonyvidék (Közép-Dunántúl, Magyarország)",
        ),
    ),
    Landscape(
        osmId = "279663379",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_gerecse_hegység,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        center = Location(47.6177834, 18.5489089),
        kirandulastippekTag = "vertes-gerecse-velencei-to",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598247",
            areaName = "Gerecse (Hegycsoport)",
        ),
    ),
    Landscape(
        osmId = "279665573",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_keszthelyi_hegység,
        landscapeType = LandscapeType.MOUNTAIN_WITH_LAKE,
        center = Location(46.8503130, 17.2709995),
        kirandulastippekTag = "keszthely-es-kornyeke",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598291",
            areaName = "Keszthelyi-hegység (Hegycsoport)",
        ),
    ),
    Landscape(
        osmId = "279590728",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_cserhát,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        center = Location(47.8788179, 19.4148133),
        kirandulastippekTag = "palocfold",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359979",
            areaName = "Cserhátvidék (Észak-Magyarország, Magyarország)",
        ),
    ),
    Landscape(
        osmId = "279593581",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_heves_Borsodi_dombság,
        landscapeType = LandscapeType.MOUNTAIN_LOW,
        center = Location(48.1220709, 20.1976971),
        kirandulastippekTag = null,
        termeszetjaroTag = null,
    ),
    Landscape(
        osmId = "279651467",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_gödöllői_dombság,
        landscapeType = LandscapeType.MOUNTAIN_LOW,
        center = Location(47.4766676, 19.4366835),
        kirandulastippekTag = "budapest-kornyeke",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "59627046",
            areaName = "Gödöllői-dombvidék (Közép-Magyarország, Magyarország)",
        ),
    ),
    Landscape(
        osmId = "279656183",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_kopasz_hegy,
        landscapeType = LandscapeType.WINE_AREA,
        center = Location(48.1260252, 21.3775780),
        kirandulastippekTag = "zemplen",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "25014010",
            areaName = "Zempléni Tájvédelmi Körzet (Tájvédelmi körzet)",
        ),
    ),
    Landscape(
        osmId = "279656184",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_zempléni_hegység,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        center = Location(48.3440651, 21.4169262),
        kirandulastippekTag = "zemplen",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "25014010",
            areaName = "Zempléni Tájvédelmi Körzet (Tájvédelmi körzet)",
        ),
    ),
    Landscape(
        osmId = "279660793",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_cserehát,
        landscapeType = LandscapeType.FOREST_AREA,
        center = Location(48.3520520, 20.9643461),
        kirandulastippekTag = "palocfold",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598216",
            areaName = "Cserehát (Hegycsoport)",
        ),
    ),
    Landscape(
        osmId = "279663918",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_vértes,
        landscapeType = LandscapeType.MOUNTAIN_LOW,
        center = Location(47.4375701, 18.3625658),
        kirandulastippekTag = "vertes-gerecse-velencei-to",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23359992",
            areaName = "Vértes és vidéke (Közép-Dunántúl, Magyarország)",
        ),
    ),
    Landscape(
        osmId = "279664160",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_velencei_hegység,
        landscapeType = LandscapeType.MOUNTAIN_WITH_LAKE,
        center = Location(47.2654220, 18.5854126),
        kirandulastippekTag = "vertes-gerecse-velencei-to",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "59627042",
            areaName = "Velencei-hegység (Közép-Dunántúl, Magyarország)",
        ),
    ),
    Landscape(
        osmId = "279666014",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_zselic,
        landscapeType = LandscapeType.STAR_GAZING_AREA,
        center = Location(46.2185793, 17.8800546),
        kirandulastippekTag = "tolna",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "25014012",
            areaName = "Zselici Tájvédelmi Körzet (Tájvédelmi körzet)",
        ),
    ),
    Landscape(
        osmId = "279667079",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_villányi_hegység,
        landscapeType = LandscapeType.WINE_AREA,
        center = Location(45.8827512, 18.2730710),
        kirandulastippekTag = "pecs-baranya",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598286",
            areaName = "Villányi-hegység (Hegycsoport)",
        ),
    ),
    Landscape(
        osmId = "300323308",
        osmType = PlaceType.WAY,
        nameRes = R.string.landscape_kőszegi_hegység,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        center = Location(47.3207697, 16.4054968),
        kirandulastippekTag = "koszeg-es-szombathely-kornyeke",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "25013914",
            areaName = "Kőszegi Tájvédelmi Körzet (Tájvédelmi körzet)",
        ),
    ),
    Landscape(
        osmId = "11073175",
        osmType = PlaceType.RELATION,
        nameRes = R.string.landscape_soproni_hegység,
        landscapeType = LandscapeType.MOUNTAIN_MEDIUM,
        center = Location(47.6538256, 16.4890472),
        kirandulastippekTag = "sopron-es-kornyeke",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "23598250",
            areaName = "Soproni-hegység (Hegyvonulat)",
        ),
    ),
    Landscape(
        osmId = "6503266",
        osmType = PlaceType.RELATION,
        nameRes = R.string.landscape_hortobágy,
        landscapeType = LandscapeType.PLAIN_LAND,
        center = Location(47.49350, 21.05344),
        kirandulastippekTag = "hortobagy-tisza-to-debrecen",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "32258164",
            areaName = "Hortobágyi Nemzeti Park (Nemzeti Park)",
        ),
    ),
    Landscape(
        osmId = "14364597",
        osmType = PlaceType.RELATION,
        nameRes = R.string.landscape_őrség,
        landscapeType = LandscapeType.PLAIN_LAND,
        center = Location(46.83921, 16.40093),
        kirandulastippekTag = "orseg",
        termeszetjaroTag = TermeszetjaroTag(
            areaId = "32258166",
            areaName = "Őrségi Nemzeti Park (Nemzeti Park)",
        ),
    ),
)
