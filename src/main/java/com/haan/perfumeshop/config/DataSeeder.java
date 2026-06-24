package com.haan.perfumeshop.config;

import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.model.Perfume;
import com.haan.perfumeshop.model.PerfumeVariant;
import com.haan.perfumeshop.repository.UserRepository;
import com.haan.perfumeshop.repository.PerfumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PerfumeRepository perfumeRepository;

    @Override
    public void run(String... args) throws Exception {

        // 1. Khởi tạo tài khoản Admin mặc định
        if (userRepository.findByEmail("admin@haan.vn").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@haan.vn");
            admin.setPassword("123456");
            admin.setRole("ADMIN");

            userRepository.save(admin);
            System.out.println("✅ Đã khởi tạo tài khoản Admin mặc định!");
        }

        // 2. Khởi tạo 50 sản phẩm nước hoa nếu cơ sở dữ liệu hiện tại ít hơn 20 sản phẩm
        if (perfumeRepository.count() < 20) {
            System.out.println("🌱 Bắt đầu thiết kế thêm 50 sản phẩm nước hoa chất lượng cao...");

            // Danh sách các hình ảnh có sẵn trong thư mục static/uploads
            String imgBleu = "eb59436f-6678-44b2-9656-cf557ff91d49_Bleu de Chane.jpeg";
            String imgPoison = "10000447-7a16-45b3-9a35-f6c2006bff3d_Nước Hoa Poison Girl Dior for women.jpg";
            String imgHermes = "2c1f85f0-6274-44c1-9c0a-d10a1c16e510_hermes-l-ambre-des-merveilles-768x614.jpg";
            String imgLancome = "05099940-0e5f-4145-ab2e-fba6fbe2078d_Lancome-Tresor-Midnight-Rose-3-768x768.jpg";
            String imgLeLabo = "82d4af41-5bd7-4016-a29d-5c4bdd115ca4_L-e-La-bo-Ano-ther-1-3-768x768.png";
            String imgJpg = "3a2e324c-dbc2-4fc7-be9c-ccb9cbf98ee5_jean-paul-gaultier-le-beau-paradise-garden-75ml.jpg";
            String imgBossPatchouli = "ff5571cc-1377-4a9f-8497-877e1321eac8_Nước Hoa Boss The Collection Cashmere Patchouli.jpg";
            String imgLvBeach = "0700b789-4065-4c2a-9427-23bd0f1ebb8c_O-n-T-he-Be-ach-10ml-768x768.png";
            String imgDiptyque = "52342992-bff8-44c7-a35b-c9d2149e55bf_Nuoc-hoa-Diptyque-Fleur-de-Peau-e1675071937586.png";
            String imgBossExtreme = "5436bf9a-1319-493e-ac97-007a1824fa14_Hugo-Extreme-Hugo-Boss-2-680x800.jpg";
            String imgCafeRose = "55737b12-e511-4a00-9773-2d417eccab31_thiet-ke-ca-fe-ro-se-50ml-768x561.png";
            String imgLvDream = "626989d1-a0e8-441c-b449-c7f8048f3649_California Dream EDP.jpeg";
            String imgDiorFahr = "6659d0b8-c5c6-4cb3-afa9-55447bfbcee3_Dior-Fahrenheie-Absolute-768x768.jpg";
            String imgYslNuit = "89f6710b-5e1d-4bed-ae5f-764e77af3366_yves-saint-laurent-la-nuit-de-lhomme-le-parfum-edp_1.jpg";
            String imgGucci = "91870e64-277f-4eb2-a656-e18a43b7d080_Gucci Premiere.jpg";
            String imgDgBlue = "a86ef377-11e2-45be-a536-c48be3dad902_Light-Blue-Living-Stromboli-1-1.jpg";
            String imgLvCactus = "d9f4c32f-0524-4d91-a15a-3ac4f0732729_Louis-Vuitton-Cactus-Garden-5.jpg";

            // 1. Chanel Bleu de Chanel Parfum
            createPerfume("Chanel Bleu de Chanel Parfum", "Chanel", "Hương Gỗ Thơm",
                    "Bleu de Chanel Parfum mang hương thơm gỗ nồng nàn và đẳng cấp cho phái mạnh, đại diện cho tự do và bản lĩnh kiên định.",
                    "Lịch lãm, Sang trọng, Nam tính", "Lâu - 8 giờ đến 12 giờ", "Gần - Trong vòng một cánh tay", "Pháp", "Nam", imgBleu,
                    new String[][]{{"10ml", "450000", "50"}, {"50ml", "2400000", "30"}, {"100ml", "3900000", "20"}});

            // 2. Dior Sauvage Elixir
            createPerfume("Dior Sauvage Elixir", "Dior", "Hương Gỗ Cay Nồng",
                    "Dior Sauvage Elixir là phiên bản nước hoa nam đậm đặc bậc nhất với các nốt hương gia vị nồng nàn, oải hương và gỗ ấm áp.",
                    "Mạnh mẽ, Cuốn hút, Bí ẩn", "Rất lâu - Trên 12 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Pháp", "Nam", imgDiorFahr,
                    new String[][]{{"10ml", "550000", "40"}, {"60ml", "3100000", "25"}, {"100ml", "4300000", "15"}});

            // 3. YSL Libre Eau de Parfum
            createPerfume("YSL Libre Eau de Parfum", "YSL", "Hương Hoa Cỏ Phương Đông",
                    "YSL Libre là sự kết hợp táo bạo giữa hoa oải hương Pháp quyến rũ quyện cùng sự ngọt ngào của hoa cam Maroc kiêu kỳ.",
                    "Sang trọng, Tự do, Quyến rũ", "Lâu - 7 giờ đến 12 giờ", "Gần - Trong vòng một cánh tay", "Pháp", "Nữ", imgYslNuit,
                    new String[][]{{"10ml", "390000", "60"}, {"50ml", "2150000", "35"}, {"100ml", "3200000", "20"}});

            // 4. Gucci Bloom For Women EDP
            createPerfume("Gucci Bloom For Women EDP", "Gucci", "Hương Hoa Cỏ",
                    "Gucci Bloom tái hiện khu vườn rực rỡ ngập tràn sắc hoa huệ, hoa nhài tinh tế kết hợp cùng hoa kim ngân ngọt dịu.",
                    "Tinh tế, Nữ tính, Quý phái", "Tạm ổn - 6 giờ đến 8 giờ", "Gần - Trong vòng một cánh tay", "Ý", "Nữ", imgGucci,
                    new String[][]{{"10ml", "380000", "45"}, {"50ml", "1950000", "25"}, {"100ml", "2850000", "20"}});

            // 5. Creed Aventus For Men
            createPerfume("Creed Aventus For Men", "Creed", "Hương Trái Cây Đậm Gỗ",
                    "Creed Aventus là biểu tượng hoàng gia, khẳng định vị thế của phái mạnh bằng nốt hương dứa chín và khói gỗ bạch dương nam tính.",
                    "Đẳng cấp, Quyền lực, Thành đạt", "Lâu - 8 giờ đến 12 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Pháp", "Nam", imgBleu,
                    new String[][]{{"10ml", "750000", "30"}, {"50ml", "4200000", "15"}, {"120ml", "6800000", "10"}});

            // 6. Hermes Terre d'Hermes EDT
            createPerfume("Terre d'Hermes Eau de Toilette", "Hermes", "Hương Gỗ Cay Nồng",
                    "Terre d'Hermes là sự cân bằng tuyệt hảo giữa hương cam bưởi tươi mát, khoáng chất của đất ấm và sự trầm mặc từ gỗ tuyết tùng.",
                    "Phong trần, Lịch lãm, Tự nhiên", "Lâu - 7 giờ đến 10 giờ", "Gần - Trong vòng một cánh tay", "Pháp", "Nam", imgHermes,
                    new String[][]{{"10ml", "350000", "50"}, {"50ml", "1850000", "30"}, {"100ml", "2650000", "25"}});

            // 7. Le Labo Santal 33
            createPerfume("Le Labo Santal 33 EDP", "Le Labo", "Hương Gỗ Thơm",
                    "Santal 33 huyền thoại mang đậm dấu ấn gỗ đàn hương, thảo mộc, khói da thuộc hoang dã phù hợp cho cả nam lẫn nữ cá tính.",
                    "Độc đáo, Cá tính, Nghệ thuật", "Rất lâu - Trên 12 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Mỹ", "Unisex", imgLeLabo,
                    new String[][]{{"10ml", "790000", "40"}, {"50ml", "3800000", "20"}, {"100ml", "5800000", "15"}});

            // 8. Kilian Angels' Share EDP
            createPerfume("Kilian Angels' Share EDP", "Kilian", "Hương Vani Phương Đông",
                    "Nước hoa quý tộc Angels' Share mang vị ngọt ngào từ rượu Cognac thượng hạng kết hợp vỏ quế ấm nóng và đậu Tonka béo ngậy.",
                    "Ngọt ngào, Quý tộc, Say đắm", "Rất lâu - Trên 12 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Pháp", "Unisex", imgCafeRose,
                    new String[][]{{"10ml", "850000", "30"}, {"50ml", "4950000", "15"}});

            // 9. Tom Ford Lost Cherry EDP
            createPerfume("Tom Ford Lost Cherry EDP", "Tom Ford", "Hương Trái Cây Hoa Cỏ",
                    "Lost Cherry là hành trình quyến rũ ngọt ngào từ quả anh đào chín mọng cùng hạnh nhân đắng, điểm xuyết hoa hồng Thổ Nhĩ Kỳ tinh xảo.",
                    "Quyến rũ, Khiêu gợi, Đậm đà", "Lâu - 7 giờ đến 10 giờ", "Gần - Trong vòng một cánh tay", "Mỹ", "Unisex", imgPoison,
                    new String[][]{{"10ml", "890000", "30"}, {"50ml", "5200000", "15"}, {"100ml", "8200000", "10"}});

            // 10. Maison Francis Kurkdjian Baccarat Rouge 540 Extrait
            createPerfume("MFK Baccarat Rouge 540 Extrait", "MFK", "Hương Hoa Cỏ Gỗ",
                    "Kiệt tác Baccarat Rouge 540 Extrait lấp lánh với hương nghệ tây quý hiếm kết hợp nhựa thông ấm áp cùng long diên hương sang trọng.",
                    "Xa hoa, Tinh tế, Đỉnh cao", "Rất lâu - Trên 12 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Pháp", "Unisex", imgCafeRose,
                    new String[][]{{"10ml", "890000", "40"}, {"70ml", "6200000", "12"}, {"200ml", "12500000", "5"}});

            // 11. Jean Paul Gaultier Le Male Le Parfum
            createPerfume("Jean Paul Gaultier Le Male Le Parfum", "Jean Paul Gaultier", "Hương Phương Đông Ấm Áp",
                    "Thiết kế chai thân hình nam tính lịch lãm quyện cùng oải hương Pháp, thảo mộc ấm sực gợi cảm cùng hương vani nam tính quyến rũ.",
                    "Gợi cảm, Cuốn hút, Ấm áp", "Lâu - 8 giờ đến 12 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Pháp", "Nam", imgJpg,
                    new String[][]{{"10ml", "380000", "50"}, {"75ml", "2100000", "30"}, {"125ml", "2850000", "20"}});

            // 12. Lancome La Vie Est Belle EDP
            createPerfume("Lancome La Vie Est Belle EDP", "Lancome", "Hương Hoa Cỏ Trái Cây",
                    "La Vie Est Belle mang ý nghĩa 'Cuộc sống tươi đẹp', chứa đựng hương quả lê chín mọng, kẹo ngọt Praline tinh tế và hoắc hương ấm áp.",
                    "Ngọt ngào, Hạnh phúc, Nữ tính", "Lâu - 8 giờ đến 12 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Pháp", "Nữ", imgLancome,
                    new String[][]{{"10ml", "360000", "60"}, {"50ml", "1950000", "35"}, {"100ml", "2750000", "25"}});

            // 13. Versace Eros For Men EDT
            createPerfume("Versace Eros For Men EDT", "Versace", "Hương Thảo Mộc Phương Đông",
                    "Versace Eros được đặt theo tên của vị thần tình yêu, mang hương táo xanh sảng khoái quyện cùng bạc hà mát lạnh và vani ngọt dịu.",
                    "Trẻ trung, Sát gái, Năng động", "Lâu - 7 giờ đến 10 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Ý", "Nam", imgDgBlue,
                    new String[][]{{"10ml", "320000", "70"}, {"50ml", "1650000", "40"}, {"100ml", "2250000", "30"}});

            // 14. Bvlgari Aqva Pour Homme EDT
            createPerfume("Bvlgari Aqva Pour Homme EDT", "Bvlgari", "Hương Thơm Biển",
                    "Aqva Pour Homme khơi gợi sự phóng khoáng mát mẻ mát sảng khoái của đại dương bao la với nốt hương rong biển tươi và quýt chín mọng.",
                    "Tươi mát, Phóng khoáng, Nam tính", "Tạm ổn - 4 giờ đến 6 giờ", "Gần - Trong vòng một cánh tay", "Ý", "Nam", imgDgBlue,
                    new String[][]{{"10ml", "290000", "80"}, {"50ml", "1500000", "50"}, {"100ml", "2050000", "40"}});

            // 15. Narciso Rodriguez For Her Pure Musc EDP
            createPerfume("Narciso Rodriguez Pure Musc EDP", "Narciso Rodriguez", "Hương Hoa Cỏ Gỗ Xạ Hương",
                    "Pure Musc là hiện thân thuần khiết nhất của xạ hương trắng thanh lịch hòa cùng hương hoa dịu dàng mang vẻ đẹp sang trọng kín đáo.",
                    "Thanh lịch, Kiêu sa, Sạch sẽ", "Lâu - 7 giờ đến 10 giờ", "Gần - Trong vòng một cánh tay", "Mỹ", "Nữ", imgDiptyque,
                    new String[][]{{"10ml", "380000", "55"}, {"50ml", "2150000", "30"}, {"100ml", "2950000", "20"}});

            // 16. Jo Malone Wood Sage & Sea Salt Cologne
            createPerfume("Jo Malone Wood Sage & Sea Salt Cologne", "Jo Malone", "Hương Cam Chanh Thảo Mộc",
                    "Hương gió biển lồng lộng quyện cùng muối khoáng và cây xô thơm mộc mạc thư thái đặc trưng của Jo Malone London.",
                    "Thư thái, Nhẹ nhàng, Tinh tế", "Tạm ổn - 4 giờ đến 6 giờ", "Gần - Trong vòng một cánh tay", "Anh", "Unisex", imgDgBlue,
                    new String[][]{{"10ml", "390000", "65"}, {"30ml", "1550000", "40"}, {"100ml", "2950000", "25"}});

            // 17. Louis Vuitton California Dream EDP
            createPerfume("Louis Vuitton California Dream EDP", "Louis Vuitton", "Hương Cam Chanh Thảo Mộc",
                    "California Dream lưu giữ khoảnh khắc hoàng hôn rực rỡ vùng viễn tây với hương quýt rực nắng kết hợp xạ hương mềm mại.",
                    "Tươi sáng, Sang trọng, Thơ mộng", "Lâu - 6 giờ đến 8 giờ", "Gần - Trong vòng một cánh tay", "Pháp", "Unisex", imgLvDream,
                    new String[][]{{"10ml", "820000", "30"}, {"100ml", "7200000", "15"}});

            // 18. Yves Saint Laurent Y Eau de Parfum
            createPerfume("YSL Y Eau de Parfum", "YSL", "Hương Gỗ Thảo Mộc",
                    "Nước hoa nam YSL Y EDP đầy cá tính mạnh mẽ với nốt hương táo xanh giòn giã kết hợp xô thơm nam tính và gừng cay nồng ấm.",
                    "Hiện đại, Cuốn hút, Năng động", "Lâu - 8 giờ đến 12 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Pháp", "Nam", imgYslNuit,
                    new String[][]{{"10ml", "420000", "60"}, {"60ml", "2250000", "35"}, {"100ml", "3150000", "25"}});

            // 19. Chanel Coco Mademoiselle EDP
            createPerfume("Chanel Coco Mademoiselle EDP", "Chanel", "Hương Hoa Cỏ Phương Đông",
                    "Biểu tượng quyến rũ vượt thời gian Coco Mademoiselle mang sự tươi mát của cam quýt cùng sự quyến rũ khó cưỡng từ hoắc hương gợi cảm.",
                    "Sang trọng, Quý phái, Quyến rũ", "Lâu - 8 giờ đến 12 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Pháp", "Nữ", imgPoison,
                    new String[][]{{"10ml", "460000", "50"}, {"50ml", "2650000", "30"}, {"100ml", "3850000", "20"}});

            // 20. Dior J'adore Eau de Parfum
            createPerfume("Dior J'adore Eau de Parfum", "Dior", "Hương Hoa Cỏ Trái Cây",
                    "Hương nước hoa J'adore lộng lẫy và rực rỡ như bó hoa vàng lung linh bao bọc bởi ngọc lan tây, hoa nhài sambac quý phái.",
                    "Kiêu sa, Tinh tế, Nữ tính", "Lâu - 7 giờ đến 10 giờ", "Gần - Trong vòng một cánh tay", "Pháp", "Nữ", imgGucci,
                    new String[][]{{"10ml", "390000", "45"}, {"50ml", "2150000", "25"}, {"100ml", "3150000", "20"}});

            // 21. Hugo Boss The Collection Cashmere Patchouli
            createPerfume("Hugo Boss Cashmere Patchouli", "Hugo Boss", "Hương Gỗ Cay Nồng",
                    "Hương nước hoa đặc sắc mô phỏng chất liệu vải cashmere mịn màng, với hoắc hương nồng đượm, sô cô la ấm và gỗ trầm ấm áp.",
                    "Ấm áp, Sang trọng, Độc đáo", "Lâu - 8 giờ đến 10 giờ", "Gần - Trong vòng một cánh tay", "Đức", "Nam", imgBossPatchouli,
                    new String[][]{{"10ml", "420000", "40"}, {"100ml", "3600000", "20"}});

            // 22. Diptyque Fleur de Peau EDP
            createPerfume("Diptyque Fleur de Peau EDP", "Diptyque", "Hương Hoa Cỏ Gỗ Xạ Hương",
                    "Fleur de Peau mang hương da thịt mềm mại, hương phấn thơm hoài cổ từ diên vĩ quý giá quyện lẫn xạ hương đầy gọi mời thanh tao.",
                    "Thanh nhã, Tự nhiên, Quyến rũ", "Lâu - 8 giờ đến 10 giờ", "Gần - Trong vòng một cánh tay", "Pháp", "Unisex", imgDiptyque,
                    new String[][]{{"10ml", "490000", "30"}, {"75ml", "3450000", "20"}});

            // 23. Hugo Boss Extreme For Men EDP
            createPerfume("Hugo Extreme Hugo Boss", "Hugo Boss", "Hương Cam Chanh Thảo Mộc",
                    "Nước hoa Hugo Boss Extreme năng động, mang đến luồng không khí mát lạnh tức thì với táo xanh tươi, thảo mộc dạt dào sức sống.",
                    "Khỏe khoắn, Thể thao, Năng động", "Tạm ổn - 6 giờ đến 8 giờ", "Gần - Trong vòng một cánh tay", "Đức", "Nam", imgBossExtreme,
                    new String[][]{{"10ml", "280000", "60"}, {"100ml", "1950000", "30"}});

            // 24. Montale Cafe Intense (Cafe Rose Style)
            createPerfume("Montale Intense Cafe EDP", "Maison Francis Kurkdjian", "Hương Hoa Hồng Cà Phê",
                    "Intense Cafe là ly cà phê đen sánh đặc ngào ngạt hương thơm quyện lẫn những cánh hoa hồng Pháp đỏ rực lãng mạn và hổ phách ấm ngọt.",
                    "Say đắm, Nồng nàn, Thu hút", "Lâu - 8 giờ đến 12 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Pháp", "Unisex", imgCafeRose,
                    new String[][]{{"10ml", "350000", "50"}, {"100ml", "2450000", "30"}});

            // 25. Dolce & Gabbana Light Blue Intense Pour Homme
            createPerfume("D&G Light Blue Intense Pour Homme", "Dolce & Gabbana", "Hương Cam Chanh Thơm Mát",
                    "Light Blue Intense Pour Homme khắc họa biển Địa Trung Hải xanh ngắt tràn đầy nắng gió với bưởi chua thanh và muối biển mát rượi.",
                    "Tươi mát, Thể thao, Phóng khoáng", "Lâu - 7 giờ đến 9 giờ", "Gần - Trong vòng một cánh tay", "Ý", "Nam", imgDgBlue,
                    new String[][]{{"10ml", "310000", "70"}, {"50ml", "1650000", "45"}, {"100ml", "2150000", "35"}});

            // 26. Louis Vuitton Cactus Garden EDP
            createPerfume("Louis Vuitton Cactus Garden", "Louis Vuitton", "Hương Thảo Mộc Xanh",
                    "Cactus Garden khơi gợi không gian mát mẻ vùng nhiệt đới với trà mate Nam Mỹ, cam bergamot Calabria tươi sáng và sả chanh thanh mát.",
                    "Mộc mạc, Xanh mát, Thư giãn", "Lâu - 6 giờ đến 8 giờ", "Gần - Trong vòng một cánh tay", "Pháp", "Unisex", imgLvCactus,
                    new String[][]{{"10ml", "820000", "30"}, {"100ml", "7200000", "15"}});

            // 27. Louis Vuitton On The Beach EDP
            createPerfume("Louis Vuitton On The Beach EDP", "Louis Vuitton", "Hương Thơm Biển",
                    "Hương cam yuzu Nhật Bản mát rượi quyện hương cát nóng ấm và thảo mộc tự nhiên vùng biển Thái Bình Dương thơ mộng.",
                    "Mát lạnh, Sang trọng, Nhẹ nhàng", "Tạm ổn - 6 giờ đến 8 giờ", "Gần - Trong vòng một cánh tay", "Pháp", "Unisex", imgLvBeach,
                    new String[][]{{"10ml", "820000", "30"}, {"100ml", "7200000", "15"}});

            // 28. Lancome Tresor Midnight Rose EDP
            createPerfume("Lancome Tresor Midnight Rose", "Lancome", "Hương Hoa Cỏ Gỗ",
                    "Tresor Midnight Rose lãng mạn như câu chuyện tình Paris với quả mâm xôi chín đỏ ngọt ngào và đóa hoa hồng đen say đắm ẩn hiện.",
                    "Lãng mạn, Ngọt ngào, Gợi cảm", "Lâu - 8 giờ đến 10 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Pháp", "Nữ", imgLancome,
                    new String[][]{{"10ml", "390000", "50"}, {"75ml", "2450000", "30"}});

            // 29. Prada L'Homme EDP
            createPerfume("Prada L'Homme EDP", "Prada", "Hương Hoa Diên Vĩ Xạ Hương",
                    "Prada L'Homme là hương thơm nam văn phòng thanh lịch, sạch sẽ bậc nhất như chiếc áo sơ mi trắng tinh khôi nhờ hoa diên vĩ cùng hổ phách.",
                    "Sạch sẽ, Thanh lịch, Lịch sự", "Lâu - 7 giờ đến 10 giờ", "Gần - Trong vòng một cánh tay", "Ý", "Nam", imgDiptyque,
                    new String[][]{{"10ml", "380000", "50"}, {"100ml", "2750000", "30"}});

            // 30. Valentino Donna Born In Roma EDP
            createPerfume("Valentino Donna Born In Roma EDP", "Valentino", "Hương Vani Hoa Cỏ",
                    "Born In Roma mang vẻ đẹp hiện đại và nổi loạn của Rome với hương lý chua đen ngọt dịu kết hợp ba loại oải hương và vani Bourbon.",
                    "Cá tính, Sang trọng, Quyến rũ", "Lâu - 8 giờ đến 10 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Ý", "Nữ", imgPoison,
                    new String[][]{{"10ml", "420000", "50"}, {"100ml", "3250000", "20"}});

            // 31. Tom Ford Oud Wood EDP
            createPerfume("Tom Ford Oud Wood EDP", "Tom Ford", "Hương Gỗ Trầm Ấm",
                    "Oud Wood của Tom Ford là một trong những hương gỗ đắt đỏ nhất thế giới, chứa đựng sự huyền bí của trầm hương, đàn hương và gia vị.",
                    "Bí ẩn, Đẳng cấp, Ấm áp", "Lâu - 8 giờ đến 10 giờ", "Gần - Trong vòng một cánh tay", "Mỹ", "Unisex", imgBleu,
                    new String[][]{{"10ml", "750000", "40"}, {"50ml", "3950000", "20"}, {"100ml", "5900000", "15"}});

            // 32. Giorgio Armani Acqua Di Gio Profondo EDP
            createPerfume("Acqua Di Gio Profondo EDP", "Giorgio Armani", "Hương Thơm Biển",
                    "Giò Xanh Profondo là hơi thở sảng khoái sâu thẳm của biển cả với hương biển khoáng chất mát lạnh kết hợp thảo mộc hương thảo nam tính.",
                    "Tươi mát, Phóng khoáng, Cuốn hút", "Lâu - 7 giờ đến 9 giờ", "Gần - Trong vòng một cánh tay", "Ý", "Nam", imgDgBlue,
                    new String[][]{{"10ml", "360000", "60"}, {"75ml", "2150000", "35"}, {"125ml", "2850000", "25"}});

            // 33. Narciso Rodriguez For Her EDP (Chai Hồng)
            createPerfume("Narciso Rodriguez For Her EDP", "Narciso Rodriguez", "Hương Hoa Cỏ Gỗ Xạ Hương",
                    "Narciso Rodriguez For Her hồng pastel kinh điển mang đậm xạ hương lôi cuốn, đào chín ngọt mượt và hoa hồng kiêu kỳ quyến rũ.",
                    "Gợi cảm, Nữ tính, Quyến rũ", "Lâu - 7 giờ đến 10 giờ", "Gần - Trong vòng một cánh tay", "Mỹ", "Nữ", imgDiptyque,
                    new String[][]{{"10ml", "360000", "60"}, {"50ml", "1950000", "30"}, {"100ml", "2750000", "25"}});

            // 34. Burberry Her London Dream EDP
            createPerfume("Burberry Her London Dream EDP", "Burberry", "Hương Hoa Cỏ Trái Cây",
                    "Burberry Her London Dream hiện đại và tự nhiên với hương chanh vàng gừng tươi sáng, hoa mẫu đơn lãng mạn lấp lánh.",
                    "Trẻ trung, Lãng mạn, Tươi tắn", "Tạm ổn - 6 giờ đến 8 giờ", "Gần - Trong vòng một cánh tay", "Anh", "Nữ", imgLancome,
                    new String[][]{{"10ml", "340000", "55"}, {"100ml", "2650000", "30"}});

            // 35. Versace Bright Crystal Absolu EDP
            createPerfume("Versace Bright Crystal Absolu EDP", "Versace", "Hương Hoa Cỏ Trái Cây",
                    "Phiên bản Bright Crystal Absolu quyến rũ mạnh mẽ hơn với quả lựu chín ngọt mọng nước cùng hoa sen thanh khiết tao nhã.",
                    "Nữ tính, Ngọt dịu, Rực rỡ", "Lâu - 7 giờ đến 9 giờ", "Gần - Trong vòng một cánh tay", "Ý", "Nữ", imgPoison,
                    new String[][]{{"10ml", "320000", "70"}, {"50ml", "1650000", "40"}, {"90ml", "2150000", "30"}});

            // 36. Paco Rabanne One Million Lucky EDT
            createPerfume("Paco Rabanne One Million Lucky", "Paco Rabanne", "Hương Gỗ Ngọt Ngào",
                    "One Million Lucky đầy mê hoặc với nốt hạt dẻ nướng thơm bùi quyện lẫn mật ong ngọt lịm và gỗ tuyết tùng nam tính.",
                    "Cuốn hút, Ngọt ngào, Nổi bật", "Lâu - 7 giờ đến 10 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Tây Ban Nha", "Nam", imgJpg,
                    new String[][]{{"10ml", "340000", "60"}, {"100ml", "2450000", "30"}});

            // 37. Kilian Black Phantom Memento Mori EDP
            createPerfume("Kilian Black Phantom EDP", "Kilian", "Hương Cà Phê Rượu Rum",
                    "Black Phantom độc đáo tột cùng với hương rượu rum cay nồng ấm quyện cùng sô cô la đắng, caramel béo ngậy đầy bí ẩn ma mị.",
                    "Bí ẩn, Cá tính, Đẳng cấp", "Rất lâu - Trên 12 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Pháp", "Unisex", imgCafeRose,
                    new String[][]{{"10ml", "850000", "35"}, {"50ml", "4950000", "15"}});

            // 38. Bvlgari Aqva Amara Pour Homme EDT
            createPerfume("Bvlgari Aqva Amara EDT", "Bvlgari", "Hương Cam Chanh Biển",
                    "Aqva Amara mang hương quýt Sicily ngập nắng Địa Trung Hải mát lạnh, trầm hương ấm sực nam tính tràn đầy sức sống.",
                    "Khỏe khoắn, Nắng gió, Tươi mát", "Lâu - 7 giờ đến 9 giờ", "Gần - Trong vòng một cánh tay", "Ý", "Nam", imgDgBlue,
                    new String[][]{{"10ml", "320000", "50"}, {"100ml", "2350000", "25"}});

            // 39. Jo Malone English Pear & Freesia Cologne
            createPerfume("Jo Malone English Pear & Freesia", "Jo Malone", "Hương Trái Cây Hoa Cỏ",
                    "Hương nước hoa biểu tượng Jo Malone tái hiện mùa thu nước Anh với quả lê chín mọng và đóa hoa lan nam phi trắng thanh nhã.",
                    "Thanh lịch, Nhẹ nhàng, Thơ mộng", "Tạm ổn - 4 giờ đến 6 giờ", "Gần - Trong vòng một cánh tay", "Anh", "Nữ", imgGucci,
                    new String[][]{{"10ml", "390000", "60"}, {"30ml", "1550000", "40"}, {"100ml", "2950000", "25"}});

            // 40. Calvin Klein CK One EDT
            createPerfume("Calvin Klein CK One EDT", "Calvin Klein", "Hương Cam Chanh Thơm Mát",
                    "CK One là dòng nước hoa unisex huyền thoại dành cho giới trẻ, vô cùng dễ chịu với hương chanh bergamot, dứa chín mát rượi.",
                    "Trẻ trung, Đơn giản, Thân thiện", "Tạm ổn - 4 giờ đến 6 giờ", "Gần - Trong vòng một cánh tay", "Mỹ", "Unisex", imgDgBlue,
                    new String[][]{{"10ml", "220000", "100"}, {"100ml", "1050000", "50"}, {"200ml", "1550000", "30"}});

            // 41. Creed Silver Mountain Water EDP
            createPerfume("Creed Silver Mountain Water", "Creed", "Hương Trà Xanh Cam Chanh",
                    "Silver Mountain Water gợi lên vẻ đẹp mát rượi trong vắt của dòng suối nguồn chảy qua dãy núi Alps tuyết trắng phủ quanh.",
                    "Tinh tế, Sang trọng, Quý phái", "Lâu - 6 giờ đến 8 giờ", "Gần - Trong vòng một cánh tay", "Pháp", "Unisex", imgLeLabo,
                    new String[][]{{"10ml", "690000", "30"}, {"100ml", "5950000", "15"}});

            // 42. Jean Paul Gaultier Ultra Male Intense EDT
            createPerfume("JPG Ultra Male Intense EDT", "Jean Paul Gaultier", "Hương Vani Phương Đông",
                    "Ultra Male là dòng nước hoa đi tiệc đầy khiêu khích với quả lê ngâm ngọt ngào, vani đậm đặc kết hợp bạc hà mát rượi quyến rũ.",
                    "Gợi cảm, Nổi bật, Sát gái", "Lâu - 8 giờ đến 12 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Pháp", "Nam", imgJpg,
                    new String[][]{{"10ml", "380000", "60"}, {"125ml", "2650000", "30"}});

            // 43. Hermes Un Jardin Sur Le Nil EDT
            createPerfume("Hermes Un Jardin Sur Le Nil", "Hermes", "Hương Hoa Cỏ Trái Cây",
                    "Khu vườn bên sông Nile đầy sức sống với xoài xanh chua giòn, hoa sen thanh khiết đượm hương phù sa lãng mạn yên bình.",
                    "Tươi mát, Mộc mạc, Thư giãn", "Tạm ổn - 6 giờ đến 8 giờ", "Gần - Trong vòng một cánh tay", "Pháp", "Unisex", imgHermes,
                    new String[][]{{"10ml", "350000", "55"}, {"100ml", "2650000", "30"}});

            // 44. YSL La Nuit De L'Homme EDT
            createPerfume("YSL La Nuit De L'Homme EDT", "YSL", "Hương Gia Vị Ấm Áp",
                    "La Nuit De L'Homme đầy nam tính quyến rũ bí ẩn cho những buổi hẹn hò lãng mạn đêm tối với bạch đậu khấu và oải hương.",
                    "Lãng mạn, Cuốn hút, Sát gái", "Tạm ổn - 6 giờ đến 8 giờ", "Gần - Trong vòng một cánh tay", "Pháp", "Nam", imgYslNuit,
                    new String[][]{{"10ml", "390000", "50"}, {"100ml", "2750000", "30"}});

            // 45. Dior Sauvage EDP
            createPerfume("Dior Sauvage Eau de Parfum", "Dior", "Hương Gỗ Thơm",
                    "Dior Sauvage EDP hoang dã tự do, nam tính cực độ với hương cam bergamot Calabria tươi sáng và tiêu Tứ Xuyên nồng ấm bí ẩn.",
                    "Mạnh mẽ, Phóng khoáng, Cuốn hút", "Lâu - 8 giờ đến 12 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Pháp", "Nam", imgDiorFahr,
                    new String[][]{{"10ml", "390000", "70"}, {"60ml", "2250000", "40"}, {"100ml", "3150000", "30"}});

            // 46. Chanel Allure Homme Sport Eau Extreme
            createPerfume("Chanel Allure Homme Sport Extreme", "Chanel", "Hương Gỗ Thảo Mộc",
                    "Allure Homme Sport Extreme vô cùng mạnh mẽ thể thao với bạc hà sảng khoái kết hợp tiêu đen cay nồng và đậu tonka ấm ngọt.",
                    "Khỏe khoắn, Năng động, Nam tính", "Lâu - 7 giờ đến 10 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Pháp", "Nam", imgBleu,
                    new String[][]{{"10ml", "450000", "50"}, {"100ml", "3750000", "25"}});

            // 47. Valentino Uomo Born In Roma Intense
            createPerfume("Valentino Uomo Born In Roma Intense", "Valentino", "Hương Phương Đông Ấm Áp",
                    "Born In Roma Intense dành cho nam đầy quyến rũ nổi loạn với gừng cay nồng ấm quyện cùng cỏ hương bài mộc mạc ngọt dịu vani.",
                    "Hiện đại, Cuốn hút, Gợi cảm", "Lâu - 8 giờ đến 10 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Ý", "Nam", imgJpg,
                    new String[][]{{"10ml", "420000", "40"}, {"100ml", "3150000", "20"}});

            // 48. Giorgio Armani Acqua Di Gio Gio Thường EDT
            createPerfume("Acqua Di Gio Pour Homme EDT", "Giorgio Armani", "Hương Thơm Biển",
                    "Giò Trắng kinh điển gắn liền với ký ức tươi mát của phái mạnh, chứa đựng nắng gió Địa Trung Hải cùng chanh tươi tắn.",
                    "Tươi mát, Thân thiện, Lịch lãm", "Tạm ổn - 4 giờ đến 6 giờ", "Gần - Trong vòng một cánh tay", "Ý", "Nam", imgDgBlue,
                    new String[][]{{"10ml", "290000", "80"}, {"100ml", "2150000", "40"}});

            // 49. Gucci Guilty Pour Homme EDP
            createPerfume("Gucci Guilty Pour Homme EDP", "Gucci", "Hương Gỗ Phương Đông",
                    "Gucci Guilty EDP quyến rũ khác biệt với nốt hương hoa hồng lãng mạn kết hợp ớt đỏ cay nồng ấm và hoắc hương sang trọng.",
                    "Bí ẩn, Cá tính, Gợi cảm", "Lâu - 7 giờ đến 10 giờ", "Gần - Trong vòng một cánh tay", "Ý", "Nam", imgGucci,
                    new String[][]{{"10ml", "380000", "50"}, {"90ml", "2650000", "30"}});

            // 50. Tom Ford Noir Extreme EDP
            createPerfume("Tom Ford Noir Extreme EDP", "Tom Ford", "Hương Gia Vị Phương Đông",
                    "Noir Extreme mang đậm chất ẩm thực phương đông say đắm với quả quýt hồng chín mọng cùng bạch đậu khấu ấm nồng và kem Kulfi béo ngậy.",
                    "Ấm áp, Khiêu gợi, Sang trọng", "Lâu - 8 giờ đến 10 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Mỹ", "Nam", imgCafeRose,
                    new String[][]{{"10ml", "450000", "40"}, {"100ml", "3850000", "20"}});

            System.out.println("✅ Đã khởi tạo thành công 50 sản phẩm nước hoa cao cấp cùng các biến thể!");
        }
    }

    private void createPerfume(String ten, String hang, String nhomHuong, String moTa, String phongCach,
                               String luuHuong, String toaHuong, String xuatXu, String gioiTinh, String hinhAnh,
                               String[][] variantsData) {
        Perfume p = new Perfume();
        p.setTen_sp(ten);
        p.setThuong_hieu(hang);
        p.setNhom_huong(nhomHuong);
        p.setMo_ta(moTa);
        p.setPhong_cach(phongCach);
        p.setLuu_huong(luuHuong);
        p.setToa_huong(toaHuong);
        p.setXuat_xu(xuatXu);
        p.setGioi_tinh(gioiTinh);
        p.setHinh_anh("/uploads/" + hinhAnh);

        ArrayList<PerfumeVariant> list = new ArrayList<>();
        int totalStock = 0;
        for (String[] vData : variantsData) {
            PerfumeVariant pv = new PerfumeVariant();
            pv.setDung_tich(vData[0]);
            pv.setGia_ban(vData[1]);
            int stock = Integer.parseInt(vData[2]);
            pv.setSo_luong_ton(stock);
            pv.setPerfume(p);
            list.add(pv);
            totalStock += stock;
        }
        p.setVariants(list);
        p.setTon_kho(totalStock);

        perfumeRepository.save(p);
    }
}