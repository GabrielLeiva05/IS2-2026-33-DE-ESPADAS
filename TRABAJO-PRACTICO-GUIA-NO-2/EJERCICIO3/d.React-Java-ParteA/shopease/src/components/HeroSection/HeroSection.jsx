import React from "react";
import HeroImage from "../../assets/img/hero-img.png";

const HeroSection = () => {
  return (
    <div
      className='relative flex items-center bg-cover bg-center text-left w-full aspect-[1440/716]'
      style={{ backgroundImage: `url(${HeroImage})` }}
    >
      <main className='px-4 sm:px-10 lg:px-24 z-10'>
        <h2 className='text-xs sm:text-lg md:text-2xl text-white'>
          T-shirt / Tops
        </h2>
        <p className='mt-1 sm:mt-3 text-white sm:max-w-xl text-2xl sm:text-4xl md:text-6xl'>
          Summer Value Pack
        </p>
        <p className='mt-1 sm:mt-3 text-white sm:max-w-xl text-xs sm:text-lg md:text-2xl'>
          cool / colorful / comfy
        </p>
        <button className='border rounded mt-3 sm:mt-6 border-black hover:bg-white hover:text-black hover:border-black text-white bg-black w-28 h-8 text-xs sm:w-44 sm:h-12 sm:text-base'>
          Shop Now
        </button>
      </main>
    </div>
  );
};

export default HeroSection;