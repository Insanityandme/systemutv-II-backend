package se.myhappyplants.javalin.utils;

import java.util.HashMap;

/**
 * {
 *   "id": 62423,
 *   "common_name": "Tarovine",
 *   "slug": "monstera-adansonii",
 *   "scientific_name": "Monstera adansonii",
 *   "year": 1830,
 *   "bibliography": "Wiener Z. Kunst 4: 1028 (1830)",
 *   "author": "Schott",
 *   "status": "accepted",
 *   "rank": "species",
 *   "family_common_name": null,
 *   "genus_id": 2961,
 *   "image_url": "https://bs.plantnet.org/image/o/38d4346034e89f4e5917357f2bc62cdcd150a3af",
 *   "synonyms": [
 *     "Dracontium pertusum",
 *     "Calla dracontium",
 *     "Calla pertusa"
 *   ],
 *   "genus": "Monstera",
 *   "family": "Araceae",
 *   "links": {
 *     "self": "/api/v1/species/monstera-adansonii",
 *     "plant": "/api/v1/plants/monstera-adansonii",
 *     "genus": "/api/v1/genus/monstera"
 *   }
 * }
 */
public class TreflePlantSwaggerObject {
    public int id;
    public String common_name;
    public String slug;
    public String scientific_name;
    public int year;
    public String bibliography;
    public String author;
    public String status;
    public String rank;
    public String family_common_name = null;
    public int genus_id;
    public String image_url;
    public String[] synonyms = null;
    public String genus;
    public String family;
    public HashMap<String, String> links = new HashMap<>();

    public TreflePlantSwaggerObject() {
    }

    public TreflePlantSwaggerObject(int id, String common_name, String slug, String scientific_name, int year, String bibliography, String author, String status, String rank, String family_common_name, int genus_id, String image_url, String[] synonyms, String genus, String family, HashMap<String, String> links) {
        this.id = id;
        this.common_name = common_name;
        this.slug = slug;
        this.scientific_name = scientific_name;
        this.year = year;
        this.bibliography = bibliography;
        this.author = author;
        this.status = status;
        this.rank = rank;
        this.family_common_name = family_common_name;
        this.genus_id = genus_id;
        this.image_url = image_url;
        this.synonyms = synonyms;
        this.genus = genus;
        this.family = family;
        this.links = links;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setCommon_name(String common_name) {
        this.common_name = common_name;
    }

    public String getCommon_name() {
        return common_name;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getSlug() {
        return slug;
    }

    public void setScientific_name(String scientific_name) {
        this.scientific_name = scientific_name;
    }

    public String getScientific_name() {
        return scientific_name;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getYear() {
        return year;
    }

    public void setBibliography(String bibliography) {
        this.bibliography = bibliography;
    }

    public String getBibliography() {
        return bibliography;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getRank() {
        return rank;
    }

    public void setFamily_common_name(String family_common_name) {
        this.family_common_name = family_common_name;
    }

    public String getFamily_common_name() {
        return family_common_name;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    public String getGenus() {
        return genus;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setLinks(HashMap<String, String> links) {
        this.links = links;
    }

    public HashMap<String, String> getLinks() {
        return links;
    }

    public void setGenus_id(int genus_id) {
        this.genus_id = genus_id;
    }

    public int getGenus_id() {
        return genus_id;
    }

    public void setSynonyms(String[] synonyms) {
        this.synonyms = synonyms;
    }

    public String[] getSynonyms() {
        return synonyms;
    }
}
