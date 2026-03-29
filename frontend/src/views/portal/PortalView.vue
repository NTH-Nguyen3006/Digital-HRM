<script setup>
import { useRouter } from 'vue-router'
import PortalLayout from '../../layouts/PortalLayout.vue'
import BaseButton from '../../components/common/BaseButton.vue'
import GlassCard from '../../components/common/GlassCard.vue'
import {
  ArrowRight,
  Users,
  Clock,
  ShieldCheck,
  FileText,
  HelpCircle,
  TrendingUp,
  MessageCircle,
  Zap,
  Layout,
  Star,
  Globe
} from 'lucide-vue-next'

const router = useRouter()

const features = [
  {
    title: 'Hệ thống Quản trị Hiện đại',
    desc: 'Quản lý toàn diện vòng đời nhân viên từ tuyển dụng đến nghỉ hưu.',
    icon: Layout,
    color: 'bg-indigo-50 text-indigo-600'
  },
  {
    title: 'Tự phục vụ (ESS)',
    desc: 'Nhân viên chủ động đăng ký nghỉ phép, xem bảng lương và cập nhật hồ sơ.',
    icon: Users,
    color: 'bg-emerald-50 text-emerald-600'
  },
  {
    title: 'Chính sách Minh bạch',
    desc: 'Truy cập mọi quy định, chính sách và cẩm nang nhân viên mọi lúc mọi nơi.',
    icon: ShieldCheck,
    color: 'bg-sky-50 text-sky-600'
  },
  {
    title: 'Báo cáo thông minh',
    desc: 'Phân tích dữ liệu nhân sự đa chiều giúp đưa ra quyết định chính xác.',
    icon: TrendingUp,
    color: 'bg-rose-50 text-rose-600'
  }
]

const services = [
  { id: 1, name: 'Đăng ký nghỉ phép', icon: Clock, path: '/leaves' },
  { id: 2, name: 'Tra cứu bảng lương', icon: FileText, path: '/payroll' },
  { id: 3, name: 'Hồ sơ cá nhân', icon: Users, path: '/portal/profile' },
  { id: 4, name: 'Lịch sử chấm công', icon: Zap, path: '/portal/attendance' },
  { id: 5, name: 'Hỗ trợ kỹ thuật', icon: HelpCircle, path: '#' },
  { id: 6, name: 'Góp ý nội bộ', icon: MessageCircle, path: '#' },
]

const newsItems = [
  {
    id: 1,
    title: 'Thông báo nghỉ lễ sắp tới',
    date: '28/03/2024',
    category: 'Thông báo',
    image: 'https://placehold.co/600'
  },
  {
    id: 2,
    title: 'Chương trình đào tạo kỹ năng mới',
    date: '25/03/2024',
    category: 'Đào tạo',
    image: 'https://placehold.co/600'
  },
  {
    id: 3,
    title: 'Sổ tay nhân sự phiên bản mới 2024',
    date: '20/03/2024',
    category: 'Chính sách',
    image: 'https://placehold.co/600'
  }
]
</script>

<template>
  <PortalLayout>
    <div class="space-y-40 mb-40">
      <section
        class="max-w-7xl mx-auto px-6 pt-20 lg:pt-32 flex flex-col lg:flex-row items-center justify-between gap-16 relative">
        <div class="flex-1 space-y-10 animate-fade-in-up">
          <div
            class="inline-flex items-center px-4 py-2 rounded-2xl bg-indigo-50 text-indigo-700 text-sm font-bold border border-indigo-100/50">
            <Star class="w-4 h-4 mr-2" /> Hệ thống Quản trị Nhân sự Số hoá
          </div>
          <h1 class="text-7xl font-black text-slate-900 leading-[1.1] tracking-tighter">
            Chào mừng bạn đến với <span
              class="text-transparent bg-clip-text bg-linear-to-r from-indigo-500 to-indigo-700">COVERAGED</span>.
          </h1>
          <p class="text-xl text-slate-500 font-medium leading-relaxed max-w-xl">
            Cổng thông tin nhân sự tập trung của toàn bộ cán bộ công dân. Nơi trải nghiệm công nghệ kết nối với mục tiêu
            phát triển nguồn nhân lực bền vững.
          </p>
          <div class="flex flex-col sm:flex-row items-center space-y-4 sm:space-y-0 sm:space-x-6">
            <BaseButton size="lg" :icon="ArrowRight" iconRight @click="router.push('/login')">
              Đăng nhập
            </BaseButton>
            <div class="flex items-center space-x-4">
              <div class="flex -space-x-4">
                <div v-for="i in 3" :key="i"
                  class="w-12 h-12 rounded-full border-4 border-white bg-slate-100 flex items-center justify-center font-bold text-slate-400 text-sm">
                  U{{ i }}
                </div>
              </div>
              <p class="text-sm font-bold text-slate-700">Thêm <span class="text-indigo-600">500+</span> nhân viên đã
                tin dùng.</p>
            </div>
          </div>
        </div>

        <div class="flex-1 relative animate-scale-in">
          <!-- Abstract Graphic -->
          <div
            class="relative z-10 w-full aspect-square bg-linear-to-tr from-indigo-100 to-indigo-50 rounded-[4rem] overflow-hidden border-8 border-white shadow-2xl">
            <img src="https://placehold.co/400" alt="Work Culture"
              class="w-full h-full object-cover opacity-80 mix-blend-multiply" />
            <div class="absolute inset-0 bg-linear-to-t from-indigo-900/20 to-transparent"></div>
          </div>

          <!-- Floating elements -->
          <!-- <GlassCard padding="p-4" class="absolute -top-10 -right-10 w-48 shadow-2xl animate-float">
            <div class="flex items-center space-x-3">
              <div class="w-10 h-10 rounded-xl bg-emerald-500 flex items-center justify-center text-white">
                <Zap class="w-5 h-5" />
              </div>
              <div>
                <p class="text-xs font-bold text-slate-400">Hiệu năng</p>
                <p class="text-sm font-black text-slate-900">Tối ưu 40%</p>
              </div>
            </div>
          </GlassCard> -->

          <!-- <GlassCard padding="p-4" class="absolute -bottom-10 -left-10 w-56 shadow-2xl animate-float"
            style="animation-delay: -3s">
            <div class="flex items-center space-x-3">
              <div class="w-10 h-10 rounded-xl bg-indigo-600 flex items-center justify-center text-white">
                <Globe class="w-5 h-5" />
              </div>
              <div>
                <p class="text-xs font-bold text-slate-400">Kết nối</p>
                <p class="text-sm font-black text-slate-900">Toàn cầu hoá</p>
              </div>
            </div>
          </GlassCard> -->
        </div>

        <!-- Background Decorations -->
        <div class="absolute -top-40 -left-40 w-96 h-96 bg-indigo-50 rounded-full blur-[150px] -z-10 animate-pulse">
        </div>
      </section>

      <!-- Features Section -->
      <section id="features" class="max-w-7xl mx-auto px-6">
        <div class="text-center space-y-6 mb-24">
          <h2 class="text-5xl font-black text-slate-900 tracking-tight">Trải nghiệm khác biệt.</h2>
          <p class="text-lg text-slate-500 font-medium max-w-2xl mx-auto italic leading-relaxed">
            Chúng tôi mang đến giải pháp HRM tập trung vào hiệu suất và sự hài lòng của từng cá nhân trong tổ chức.
          </p>
        </div>

        <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
          <div v-for="f in features" :key="f.title"
            class="group p-8 rounded-[2.5rem] bg-white border border-slate-100 hover:border-indigo-100 hover:shadow-2xl hover:shadow-indigo-50 transition-all duration-500">
            <div
              :class="[f.color, 'w-14 h-14 rounded-2xl flex items-center justify-center mb-10 transition-transform group-hover:scale-110 group-hover:rotate-6']">
              <component :is="f.icon" class="w-7 h-7" />
            </div>
            <h3 class="text-xl font-bold text-slate-900 mb-4">{{ f.title }}</h3>
            <p class="text-slate-500 font-medium leading-relaxed">{{ f.desc }}</p>
          </div>
        </div>
      </section>

      <!-- Portal Services (Quick Links) -->
      <section id="services" class="bg-slate-950 py-40 px-6 relative overflow-hidden">
        <div class="absolute top-0 left-0 w-full h-full">
          <div class="absolute -top-24 -left-24 w-96 h-96 bg-indigo-600/10 rounded-full blur-[120px]"></div>
          <div class="absolute bottom-0 right-0 w-96 h-96 bg-emerald-500/10 rounded-full blur-[120px]"></div>
        </div>

        <div class="max-w-7xl mx-auto relative z-10">
          <div class="flex flex-col md:flex-row items-end justify-between gap-12 mb-24">
            <div class="space-y-6">
              <div class="text-emerald-400 font-bold uppercase tracking-widest text-sm">Self-Service Center</div>
              <h2 class="text-5xl font-black text-white tracking-tight">Dịch vụ cho nhân viên.</h2>
            </div>
            <p class="text-slate-400 font-medium text-lg max-w-md">Tiết kiệm thời gian với hệ thống tự phục vụ thông
              minh, minh bạch và tức thì.</p>
          </div>

          <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6">
            <div v-for="s in services" :key="s.id" @click="router.push(s.path)"
              class="p-8 rounded-4xl bg-white/5 border border-white/10 hover:bg-white/10 hover:border-white/20 transition-all duration-300 cursor-pointer group flex flex-col items-center text-center">
              <div
                class="w-16 h-16 rounded-2xl bg-indigo-600/20 text-indigo-400 flex items-center justify-center mb-6 group-hover:bg-indigo-600 group-hover:text-white transition-all scale-100 group-hover:scale-110">
                <component :is="s.icon" class="w-8 h-8" />
              </div>
              <span class="text-white font-bold text-sm leading-tight">{{ s.name }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- News Section -->
      <section id="news" class="max-w-7xl mx-auto px-6">
        <div class="flex items-center justify-between mb-24">
          <h2 class="text-5xl font-black text-slate-900 tracking-tight">Tin tức nội bộ.</h2>
          <BaseButton variant="outline" class="hidden sm:flex">Tất cả tin tức</BaseButton>
        </div>

        <div class="grid lg:grid-cols-3 gap-12">
          <div v-for="item in newsItems" :key="item.id" class="group cursor-pointer">
            <div class="aspect-4/3 rounded-[2.5rem] overflow-hidden mb-8 relative">
              <img :src="item.image" :alt="item.title"
                class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110" />
              <div
                class="absolute top-6 left-6 px-4 py-2 rounded-xl bg-white/90 backdrop-blur-md text-slate-900 font-bold text-xs uppercase tracking-widest">
                {{ item.category }}
              </div>
            </div>
            <div class="space-y-4">
              <div class="text-slate-400 font-bold text-sm tracking-wide">{{ item.date }}</div>
              <h3
                class="text-2xl font-black text-slate-900 leading-tight group-hover:text-indigo-600 transition-colors">
                {{ item.title }}</h3>
              <div class="flex items-center text-indigo-600 font-bold text-sm mt-6">
                Xem chi tiết
                <ArrowRight class="w-4 h-4 ml-2 group-hover:translate-x-2 transition-transform" />
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </PortalLayout>
</template>

<style scoped>
@reference "../../assets/css/main.css";

.animate-fade-in-up {
  animation: fadeInUp 1s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

.animate-scale-in {
  animation: scaleIn 1.2s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
